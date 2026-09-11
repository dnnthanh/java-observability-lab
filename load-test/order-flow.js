import http from 'k6/http';
import { check, sleep } from 'k6';
export const options = { vus: 10, duration: '30s' };
export default function () {
  const payload = JSON.stringify({customerId:'K6', productCode:'SKU-1', quantity:1, amount:9999});
  const r = http.post('http://localhost:8080/api/orders', payload, {headers:{'Content-Type':'application/json'}});
  check(r, {'created': x => x.status === 201});
  sleep(0.2);
}
