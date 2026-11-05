"""Capture native debug UI fixtures on an explicitly selected emulator, without logging in.

Install app-debug.apk on that emulator first. Example:
  py scripts/capture_ui_review.py --serial emulator-5580 --output docs/ui-review
"""
import argparse
import os
from pathlib import Path
import shutil
import subprocess
import time
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--serial', required=True)
parser.add_argument('--adb-port', type=int, default=5037)
parser.add_argument('--output', type=Path, required=True)
parser.add_argument('--preset', choices=['phone', 'compact', 'wide'], default='phone')
parser.add_argument('--font-scale', type=float, default=1.0)
parser.add_argument('--dark', action='store_true')
parser.add_argument('--ui-style', choices=['simple', 'dynamic'], default='simple')
parser.add_argument('--screens', nargs='+', default=['home', 'incoming', 'current', 'wallet', 'history', 'account'])
args = parser.parse_args()
if not args.serial.startswith(('emulator-', '127.0.0.1:', 'localhost:')):
    parser.error('UI fixtures must run on an emulator; physical devices are not supported.')

sdk = Path(os.environ.get('ANDROID_HOME', Path.home() / 'AppData/Local/Android/Sdk'))
adb = shutil.which('adb') or str(sdk / 'platform-tools/adb.exe')

def command(*parts, binary=False):
    result = subprocess.run([adb, '-P', str(args.adb_port), '-s', args.serial, *parts], capture_output=True, timeout=45, check=True)
    return result.stdout if binary else result.stdout.decode('utf-8', errors='replace')

if command('shell', 'getprop', 'ro.kernel.qemu').strip() != '1':
    parser.error('The selected target is not an emulator; refusing to change device settings.')

size, density = {'phone': ('720x1600', '320'), 'compact': ('640x1280', '320'), 'wide': ('1920x1200', '240')}[args.preset]
command('shell', 'wm', 'size', size)
command('shell', 'wm', 'density', density)
command('shell', 'settings', 'put', 'system', 'screen_off_timeout', '2147483647')
command('shell', 'svc', 'power', 'stayon', 'true')
command('shell', 'input', 'keyevent', '224')
command('shell', 'wm', 'dismiss-keyguard')
args.output.mkdir(parents=True, exist_ok=True)
for screen in args.screens:
    # Clear the activity task, keeping the already initialised application process alive.
    started = command('shell', 'am', 'start', '-W', '-f', '0x10008000', '-n', 'id.okejack.okedriver/.debug.DriverUiReviewActivity',
                      '--es', 'screen', screen, '--ef', 'font_scale', str(args.font_scale), '--ez', 'dark', str(args.dark).lower(),
                      '--es', 'ui_style', args.ui_style)
    if 'Error' in started:
        raise RuntimeError(started)
    expected = {'home': 'Mulai terima order', 'incoming': 'Order masuk', 'current': 'Geser untuk mulai',
                'wallet': 'Saldo', 'history': 'Riwayat order', 'account': 'Akun',
                'login': 'Masuk', 'login-error': 'Email atau kata sandi tidak sesuai.',
                'current-loading': 'Memuat order aktif...'}[screen]
    for attempt in range(12):
        time.sleep(2)
        command('shell', 'uiautomator', 'dump', '/sdcard/okedriver-ui-review.xml')
        hierarchy = command('shell', 'cat', '/sdcard/okedriver-ui-review.xml')
        nodes = ET.fromstring(hierarchy).iter('node')
        if any(node.get('package') == 'id.okejack.okedriver' and node.get('text') == expected for node in nodes):
            break
    else:
        raise RuntimeError(f'Expected UI for {screen} did not become visible; refusing to capture another app.')
    target = args.output / f'{screen}-{args.preset}-font{args.font_scale:g}{"-dark" if args.dark else ""}.png'
    target.write_bytes(command('exec-out', 'screencap', '-p', binary=True))
    print(target, flush=True)
