import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// k6 run -e SCENARIO=smoke   load-test.js
// k6 run -e SCENARIO=rampup  load-test.js
// k6 run -e SCENARIO=spike   load-test.js
// k6 run -e SCENARIO=soak    load-test.js

const scenarios = {
  smoke: {
    vus: 5,
    duration: '1m',
  },
  rampup: {
    stages: [
      { duration: '2m', target: 50 },
      { duration: '5m', target: 200 },
      { duration: '2m', target: 0 },
    ],
  },
  spike: {
    stages: [
      { duration: '10s', target: 10 },
      { duration: '1m',  target: 500 },
      { duration: '10s', target: 10 },
    ],
  },
  soak: {
    stages: [
      { duration: '5m',  target: 100 },
      { duration: '2h',  target: 100 },
      { duration: '5m',  target: 0 },
    ],
  },
};

const scenarioName = __ENV.SCENARIO || 'smoke';
const scenarioOpts = scenarios[scenarioName];
if (!scenarioOpts) {
  throw new Error(`Unknown SCENARIO="${scenarioName}". Available: ${Object.keys(scenarios).join(', ')}`);
}

export const options = {
  ...scenarioOpts,
  thresholds: {
    'http_req_duration{endpoint:list}':   ['p(95)<1000'],
    'http_req_duration{endpoint:create}': ['p(95)<1000'],
    'http_req_duration{endpoint:update}': ['p(95)<1000'],
    'http_req_duration{endpoint:delete}': ['p(95)<1000'],
    'http_req_failed':                    ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN    = __ENV.TOKEN;

if (!TOKEN) {
  throw new Error('Передай Yandex OAuth-токен: k6 run -e TOKEN=<token> load-test.js');
}

const headers = {
  'X-Auth-Token':  TOKEN,
  'Content-Type':  'application/json',
};

export default function () {

  // 1. Получить список заметок
  const listRes = http.get(`${BASE_URL}/api/v1/notes`, {
    headers,
    tags: { endpoint: 'list' },
  });
  check(listRes, { 'GET /notes → 200': r => r.status === 200 });

  // 2. Создать заметку
  const now = Date.now();
  const createRes = http.post(
    `${BASE_URL}/api/v1/notes`,
    JSON.stringify({
      id:         `${__VU}-${now}`,
      title:      `load-${__VU}-${now}`,
      content:    'k6 load test',
      created_at: now,
      updated_at: now,
    }),
    { headers, tags: { endpoint: 'create' } },
  );
  const created = check(createRes, { 'POST /notes → 201': r => r.status === 201 });
  if (!created) return;

  const noteId = createRes.json('id');

  // 3. Обновить заметку
  const updateRes = http.put(
    `${BASE_URL}/api/v1/notes/${noteId}`,
    JSON.stringify({ title: 'updated', content: 'k6 updated', updated_at: Date.now() }),
    { headers, tags: { endpoint: 'update' } },
  );
  check(updateRes, { 'PUT /notes/{id} → 200': r => r.status === 200 });

  // 4. Удалить заметку
  const deleteRes = http.del(
    `${BASE_URL}/api/v1/notes/${noteId}`,
    null,
    { headers, tags: { endpoint: 'delete' } },
  );
  check(deleteRes, { 'DELETE /notes/{id} → 204': r => r.status === 204 });

  sleep(1);
}
