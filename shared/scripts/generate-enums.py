from pathlib import Path
import subprocess

ENUMS_DIR = Path("shared/enums")

JAVA_OUT_ROOT = Path("shared/generated/java/enums")
PYTHON_OUT_ROOT = Path("shared/generated/python/enums")

# ---------------- PYTHON ----------------
for schema_file in ENUMS_DIR.rglob("*.json"):
    print(f"Processing {schema_file}")

    relative_path = schema_file.relative_to(ENUMS_DIR)
    python_target = (PYTHON_OUT_ROOT / relative_path).with_suffix(".py")

    python_target.parent.mkdir(parents=True, exist_ok=True)

    subprocess.run([
        "datamodel-codegen",
        "--input", str(schema_file),
        "--output", str(python_target),
    ], check=True)

# ---------------- JAVA ----------------