"""Static server for The Work Code Stitch frontend.

Serves frontend/ (index.html at /, Stitch screens at /stitch-screens/<name>/,
where each screen directory contains code.html). Standard library only.

Usage: python scripts/frontend-server.py [port]   (default 3000)
"""
import http.server
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent / "frontend"
PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 3000


class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(ROOT), **kwargs)

    def send_head(self):
        # Screen directories have code.html instead of index.html, so map
        # "/stitch-screens/<name>/" -> "/stitch-screens/<name>/code.html".
        p = Path(self.translate_path(self.path))
        if p.is_dir() and (p / "code.html").is_file():
            self.path = self.path.rstrip("/") + "/code.html"
        return super().send_head()


if __name__ == "__main__":
    os.chdir(ROOT)
    with http.server.ThreadingHTTPServer(("0.0.0.0", PORT), Handler) as httpd:
        print(f"The Work Code frontend: http://localhost:{PORT}", flush=True)
        httpd.serve_forever()
