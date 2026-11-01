import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.jsx';
import { keycloak } from './keycloak.js';
import './styles.css';

const root = document.getElementById('root');
keycloak
  .init({ onLoad: 'login-required', pkceMethod: 'S256', checkLoginIframe: false })
  .then((ok) => ok && createRoot(root).render(<App />))
  .catch((e) => { root.textContent = 'Sign-in failed. Is Keycloak running on port 8180? ' + e; });
