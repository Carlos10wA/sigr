import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 5,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<1000'],
    http_req_failed: ['rate<0.01'],
    checks: ['rate==1'],
  },
};

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const inicio = http.get(`${baseUrl}/`, {
    redirects: 0,
    timeout: '10s',
  });
  check(inicio, {
    'Inicio devuelve 200 y contiene SIGR': (r) =>
      r.status === 200 && r.body.includes('SIGR'),
  });

  const login = http.get(`${baseUrl}/login`, {
    redirects: 0,
    timeout: '10s',
  });
  check(login, {
    'Login devuelve 200 y contiene formulario': (r) =>
      r.status === 200 && r.body.includes('<form'),
  });

  sleep(1);
}