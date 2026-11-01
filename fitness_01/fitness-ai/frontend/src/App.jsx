import { useEffect, useMemo, useState } from 'react';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { keycloak } from './keycloak.js';
import { addActivity, getActivities, getProfile, getRecommendation, saveProfile } from './api.js';

const TYPES = ['RUNNING', 'WALKING', 'CYCLING', 'SWIMMING', 'STRENGTH', 'YOGA', 'HIIT', 'OTHER'];
const label = (t) => t.charAt(0) + t.slice(1).toLowerCase();
const day = (iso) => new Date(iso).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });

/* Polls until the AI service has stored a suggestion for this workout. */
function Suggestion({ activityId }) {
  const [rec, setRec] = useState(null);
  const [timedOut, setTimedOut] = useState(false);
  useEffect(() => {
    let stop = false, tries = 0, timer;
    const poll = async () => {
      try { const r = await getRecommendation(activityId); if (r && !stop) return setRec(r); } catch { /* retry */ }
      if (stop) return;
      if (++tries > 20) return setTimedOut(true);
      timer = setTimeout(poll, 2000);
    };
    poll();
    return () => { stop = true; clearTimeout(timer); };
  }, [activityId]);
  if (rec) return <p className="tip">{rec.content}</p>;
  if (timedOut) return <p className="muted">No suggestion yet. The AI service may be busy; try again shortly.</p>;
  return <p className="muted">Your coach is writing a suggestion…</p>;
}

function Dashboard({ activities }) {
  const stats = useMemo(() => ({
    count: activities.length,
    minutes: activities.reduce((s, a) => s + a.durationMinutes, 0),
    km: activities.reduce((s, a) => s + (a.distanceKm || 0), 0).toFixed(1),
    kcal: activities.reduce((s, a) => s + (a.caloriesBurned || 0), 0),
  }), [activities]);

  const daily = useMemo(() => {
    const out = [];
    for (let i = 6; i >= 0; i--) {
      const d = new Date(); d.setDate(d.getDate() - i);
      const key = d.toDateString();
      const items = activities.filter((a) => new Date(a.createdAt).toDateString() === key);
      out.push({ day: day(d), minutes: items.reduce((s, a) => s + a.durationMinutes, 0), kcal: items.reduce((s, a) => s + (a.caloriesBurned || 0), 0) });
    }
    return out;
  }, [activities]);

  const byType = useMemo(() => {
    const m = {};
    activities.forEach((a) => { m[a.type] = (m[a.type] || 0) + a.durationMinutes; });
    return Object.entries(m).map(([t, minutes]) => ({ type: label(t), minutes }));
  }, [activities]);

  if (!activities.length) return <section className="panel"><h2>Nothing logged yet</h2><p className="muted">Log your first workout to see progress charts and an AI suggestion.</p></section>;
  const latest = activities[0];
  return (
    <>
      <div className="stats">
        {[['Workouts', stats.count], ['Minutes', stats.minutes], ['Kilometres', stats.km], ['Calories', stats.kcal]].map(([k, v]) => (
          <div className="stat" key={k}><strong>{v}</strong><span>{k}</span></div>
        ))}
      </div>
      <section className="panel feature">
        <h2>Latest suggestion · {label(latest.type)}, {day(latest.createdAt)}</h2>
        <Suggestion activityId={latest.id} />
      </section>
      <div className="grid2">
        <section className="panel"><h2>Minutes, last 7 days</h2>
          <ResponsiveContainer width="100%" height={220}><BarChart data={daily}><CartesianGrid stroke="#ffffff22" vertical={false} /><XAxis dataKey="day" stroke="#c9d6d3" fontSize={12} /><YAxis stroke="#c9d6d3" fontSize={12} width={32} /><Tooltip contentStyle={{ background: '#243342', border: 0 }} /><Bar dataKey="minutes" fill="#f27a6b" radius={[4, 4, 0, 0]} /></BarChart></ResponsiveContainer></section>
        <section className="panel"><h2>Time by activity</h2>
          <ResponsiveContainer width="100%" height={220}><BarChart data={byType}><CartesianGrid stroke="#ffffff22" vertical={false} /><XAxis dataKey="type" stroke="#c9d6d3" fontSize={12} /><YAxis stroke="#c9d6d3" fontSize={12} width={32} /><Tooltip contentStyle={{ background: '#243342', border: 0 }} /><Bar dataKey="minutes" fill="#8fd6c4" radius={[4, 4, 0, 0]} /></BarChart></ResponsiveContainer></section>
      </div>
    </>
  );
}

function Workouts({ activities, reload }) {
  const [f, setF] = useState({ type: 'RUNNING', durationMinutes: 30, distanceKm: '', caloriesBurned: '', notes: '' });
  const [err, setErr] = useState(''); const [busy, setBusy] = useState(false);
  const [open, setOpen] = useState(null);
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault(); setErr(''); setBusy(true);
    try {
      const saved = await addActivity({
        type: f.type, durationMinutes: Number(f.durationMinutes),
        distanceKm: f.distanceKm === '' ? null : Number(f.distanceKm),
        caloriesBurned: f.caloriesBurned === '' ? null : Number(f.caloriesBurned),
        notes: f.notes || null,
      });
      setOpen(saved.id); setF({ ...f, distanceKm: '', caloriesBurned: '', notes: '' }); await reload();
    } catch (ex) {
      const d = ex.response?.data;
      setErr(d && typeof d === 'object' ? Object.entries(d).map(([k, v]) => `${k}: ${v}`).join('; ') : 'Could not save the workout.');
    } finally { setBusy(false); }
  };

  return (
    <div className="grid2">
      <form className="panel" onSubmit={submit}>
        <h2>Log a workout</h2>
        <label>Activity<select value={f.type} onChange={set('type')}>{TYPES.map((t) => <option key={t} value={t}>{label(t)}</option>)}</select></label>
        <label>Duration (minutes)<input type="number" min="1" max="1440" required value={f.durationMinutes} onChange={set('durationMinutes')} /></label>
        <label>Distance (km)<input type="number" step="0.1" min="0" value={f.distanceKm} onChange={set('distanceKm')} /></label>
        <label>Calories burned<input type="number" min="0" value={f.caloriesBurned} onChange={set('caloriesBurned')} /></label>
        <label>Notes<textarea rows="2" maxLength="500" value={f.notes} onChange={set('notes')} /></label>
        {err && <p className="error">{err}</p>}
        <button disabled={busy}>{busy ? 'Saving…' : 'Save workout'}</button>
      </form>
      <section className="panel">
        <h2>History</h2>
        {!activities.length && <p className="muted">No workouts yet.</p>}
        <ul className="history">
          {activities.map((a) => (
            <li key={a.id}>
              <div className="row">
                <div><b>{label(a.type)}</b> <span className="muted">{day(a.createdAt)}</span><br />
                  <span className="muted">{a.durationMinutes} min{a.distanceKm != null && ` · ${a.distanceKm} km`}{a.caloriesBurned != null && ` · ${a.caloriesBurned} kcal`}</span></div>
                <button className="ghost" type="button" onClick={() => setOpen(open === a.id ? null : a.id)}>{open === a.id ? 'Hide tip' : 'Show tip'}</button>
              </div>
              {open === a.id && <Suggestion activityId={a.id} />}
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}

function Profile() {
  const [p, setP] = useState({ fullName: '', age: '', heightCm: '', weightKg: '', fitnessGoal: '' });
  const [msg, setMsg] = useState('');
  useEffect(() => { getProfile().then((d) => d && setP({ fullName: d.fullName ?? '', age: d.age ?? '', heightCm: d.heightCm ?? '', weightKg: d.weightKg ?? '', fitnessGoal: d.fitnessGoal ?? '' })).catch(() => {}); }, []);
  const set = (k) => (e) => setP({ ...p, [k]: e.target.value });
  const num = (v) => (v === '' ? null : Number(v));
  const submit = async (e) => {
    e.preventDefault(); setMsg('');
    try { await saveProfile({ ...p, age: num(p.age), heightCm: num(p.heightCm), weightKg: num(p.weightKg) }); setMsg('Profile saved.'); }
    catch (ex) { const d = ex.response?.data; setMsg(d && typeof d === 'object' ? Object.entries(d).map(([k, v]) => `${k}: ${v}`).join('; ') : 'Could not save the profile.'); }
  };
  return (
    <form className="panel narrow" onSubmit={submit}>
      <h2>Your fitness profile</h2>
      <label>Full name<input required value={p.fullName} onChange={set('fullName')} /></label>
      <label>Age<input type="number" value={p.age} onChange={set('age')} /></label>
      <label>Height (cm)<input type="number" step="0.1" value={p.heightCm} onChange={set('heightCm')} /></label>
      <label>Weight (kg)<input type="number" step="0.1" value={p.weightKg} onChange={set('weightKg')} /></label>
      <label>Goal<input placeholder="e.g. run a 10K" value={p.fitnessGoal} onChange={set('fitnessGoal')} /></label>
      {msg && <p className={msg.endsWith('saved.') ? 'ok' : 'error'}>{msg}</p>}
      <button>Save profile</button>
    </form>
  );
}

export default function App() {
  const [tab, setTab] = useState('dashboard');
  const [activities, setActivities] = useState([]);
  const [loadErr, setLoadErr] = useState('');
  const reload = () => getActivities().then((d) => { setActivities(d); setLoadErr(''); }).catch(() => setLoadErr('Could not reach the API gateway on port 8080.'));
  useEffect(() => { reload(); }, []);
  const name = keycloak.tokenParsed?.name || keycloak.tokenParsed?.preferred_username;

  return (
    <div className="shell">
      <header>
        <h1>FitAI</h1>
        <nav>{[['dashboard', 'Dashboard'], ['workouts', 'Workouts'], ['profile', 'Profile']].map(([k, v]) =>
          <button key={k} className={tab === k ? 'tab on' : 'tab'} onClick={() => setTab(k)}>{v}</button>)}</nav>
        <div className="who"><span>{name}</span><button className="ghost" onClick={() => keycloak.logout({ redirectUri: window.location.origin })}>Sign out</button></div>
      </header>
      <main>
        {loadErr && <p className="error">{loadErr}</p>}
        {tab === 'dashboard' && <Dashboard activities={activities} />}
        {tab === 'workouts' && <Workouts activities={activities} reload={reload} />}
        {tab === 'profile' && <Profile />}
        <p className="muted foot">Suggestions are general guidance generated by AI, not medical advice.</p>
      </main>
    </div>
  );
}
