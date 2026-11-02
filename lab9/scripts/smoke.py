#!/usr/bin/env python3
"""Run real application restarts and HTTP checks against a temporary file database."""
import json
import os
from pathlib import Path
import subprocess
import tempfile
import time
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
JAVA = str(Path(os.environ['JAVA_HOME']) / 'bin/java') if os.environ.get('JAVA_HOME') else 'java'
BASE = 'http://127.0.0.1:18089'

def request(path, body=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, headers={'Content-Type': 'application/json'})
    try:
        with urllib.request.urlopen(req, timeout=2) as response:
            content = response.read()
            return response.status, json.loads(content) if content else None
    except urllib.error.HTTPError as error:
        return error.code, json.load(error)

def check_balances(alice, bob):
    status, rows = request('/api/accounts')
    assert status == 200
    balances = {row['ownerName']: row['balance'] for row in rows}
    assert balances == {'Alice': alice, 'Bob': bob}, balances

with tempfile.TemporaryDirectory(prefix='lab9-smoke-') as directory:
    for phase in (1, 2, 3):
        log_path = ROOT / 'docs' / f'startup-{phase}.log'
        args = [JAVA, '-jar', str(ROOT / 'target/lab9-1.0.0.jar'),
                '--server.port=18089', f'--spring.datasource.url=jdbc:h2:file:{directory}/txlab;DB_CLOSE_ON_EXIT=FALSE']
        if phase < 3:
            args.append('--spring.flyway.target=2')
        with log_path.open('w') as log:
            process = subprocess.Popen(args, cwd=ROOT, stdout=log, stderr=subprocess.STDOUT)
            try:
                for _ in range(150):
                    if process.poll() is not None:
                        raise RuntimeError(f'Application exited: inspect {log_path}')
                    if 'Started Lab9Application' in log_path.read_text():
                        break
                    time.sleep(0.2)
                else:
                    raise TimeoutError('Application not ready after 30 seconds')
                if phase == 1:
                    check_balances(1000, 500)
                    assert request('/api/transfers', {'fromId': 1, 'toId': 2, 'amount': 100})[0] == 204
                    check_balances(900, 600)
                    assert request('/api/transfers', {'fromId': 1, 'toId': 2, 'amount': 9999})[0] == 409
                    check_balances(900, 600)
                    assert request('/api/transfers', {'fromId': 1, 'toId': 2, 'amount': -1})[0] == 400
                    assert request('/api/transfers', {'fromId': 1, 'toId': 99, 'amount': 1})[0] == 404
                    check_balances(900, 600)
                    print('PASS HTTP: initial 1000/500; transfer 204 -> 900/600; insufficient 409 unchanged; invalid 400; missing 404', flush=True)
                else:
                    check_balances(900, 600)
                    print(f'PASS restart phase {phase}: persisted balances 900/600', flush=True)
            finally:
                process.terminate()
                try:
                    process.wait(timeout=15)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait()
        text = log_path.read_text()
        if phase == 1:
            assert 'Successfully applied 2 migrations' in text
        elif phase == 2:
            assert 'No migration necessary' in text
        else:
            assert 'Successfully applied 1 migration' in text
    print('PASS application lifecycle: fresh V1/V2; restart applies zero; next restart applies only V3', flush=True)
