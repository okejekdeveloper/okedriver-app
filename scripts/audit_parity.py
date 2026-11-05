"""Inventory Retrofit contracts and call sites without contacting the backend.

Run from any directory: py scripts/audit_parity.py [--output report.json]
Call-site detection is a static heuristic, not evidence that a feature works.
"""
import argparse
import json
import re
from pathlib import Path


def strip_comments(source):
    # Keep string literals intact (including URL strings containing //).
    pattern = r'"(?:\\.|[^"\\])*"|/\*[\s\S]*?\*/|//[^\n]*'
    return re.sub(pattern, lambda m: m[0] if m[0].startswith('"') else '', source)


def inventory(root, modern=False):
    base = root / "app/src/main/java/id/okejack/okedriver"
    sources = {path: strip_comments(path.read_text(encoding="utf-8-sig"))
               for path in base.rglob("*.kt")}
    if modern:
        sources = {p: s for p, s in sources.items() if "ui" not in p.relative_to(base).parts}
    contracts = {}
    for path, source in sources.items():
        if "interface " not in source:
            continue
        for match in re.finditer(r'@(GET|POST|PUT|DELETE|PATCH)\("([^"\n]+)"\)([\s\S]*?)\bfun\s+(\w+)\s*\(', source):
            method, url, _, name = match.groups()
            normalized_url = re.sub(r'\{[^}]+\}', '{}', url)
            key = f"{method} {normalized_url}"
            callers = [str(p.relative_to(root)).replace('\\', '/') for p, text in sources.items()
                       if p != path and re.search(r'(?:\.\s*' + re.escape(name) + r'\s*\(|::\s*' + re.escape(name) + r'\b)', text)]
            contracts.setdefault(key, []).append({"file": str(path.relative_to(root)).replace('\\', '/'),
                                                   "function": name, "callers": callers})
    return contracts


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    modern_root = Path(__file__).resolve().parents[1]
    legacy_root = modern_root.parent / "okedriver"
    legacy = inventory(legacy_root)
    modern = inventory(modern_root, modern=True)
    rows = []
    for endpoint in sorted(legacy):
        implementations = modern.get(endpoint, [])
        rows.append({"endpoint": endpoint,
                     "legacy_called": any(x["callers"] for x in legacy[endpoint]),
                     "modern_declared": bool(implementations),
                     "modern_called": any(x["callers"] for x in implementations),
                     "legacy": legacy[endpoint], "modern": implementations})
    report = {"scope": "Static Retrofit inventory. Declaration/call site does not prove runtime or UI parity.",
              "legacy_endpoints": len(legacy), "rows": rows}
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(report, indent=2), encoding="utf-8")
    print(f"Legacy contracts: {len(legacy)}; modern contracts: {len(modern)}")
    for row in rows:
        if row["legacy_called"] and not row["modern_called"]:
            label = "declaration only" if row["modern_declared"] else "missing"
            print(f"Review: {row['endpoint']} ({label})")


if __name__ == "__main__":
    main()
