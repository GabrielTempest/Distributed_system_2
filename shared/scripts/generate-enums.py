from pathlib import Path
import json
import shutil
import subprocess

# Source directories
SOURCE_ROOT = Path("shared/schemas/json")
ENUMS_DIR = SOURCE_ROOT / "enums"
MAPPING_DIR = SOURCE_ROOT / "mapping"
# Target directories
PYTHON_OUT_ROOT = Path("generated/python")
PYTHON_OUT_ENUMS = PYTHON_OUT_ROOT / "enums"
PYTHON_OUT_MAPPING = PYTHON_OUT_ROOT / "mapping"


# Clean up old generated code
if PYTHON_OUT_ROOT.exists():
    shutil.rmtree(PYTHON_OUT_ROOT)
PYTHON_OUT_ROOT.mkdir(parents=True, exist_ok=True)



# ---------------- ENUMS GENERATION ----------------
for schema_file in ENUMS_DIR.rglob("*.json"):
    relative_path = schema_file.relative_to(ENUMS_DIR)
    python_target = (PYTHON_OUT_ENUMS / relative_path).with_suffix(".py")

    python_target.parent.mkdir(parents=True, exist_ok=True)

    subprocess.run([
        "datamodel-codegen",
        "--input", str(schema_file),
        "--output", str(python_target),
    ], check=True)



# ------------------- MAPPING GENERATION ----------------
def extract_import_data(ref: str) -> tuple[str, str]:
    """ 
    Extract module and class name from a $ref string. For example, given
    "schemas/json/enums/<group>/DisasterType.json" -> "enums.<group>", "DisasterType"
    """
    path = Path(ref)
    module = ".".join(path.parts[2:-1])
    class_name = path.stem
    return f"{module}.{class_name}", class_name


def generate_code(schema_path: str, output_path: str):
    schema = json.load(open(schema_path))

    # extract data for build
    prop_enum = extract_import_data(schema["propertyNames"]["$ref"])
    item_enum = extract_import_data(
        schema["additionalProperties"]["items"]["$ref"]
    )
    instance_name = schema["title"]
    example = schema["mapping"]

    # build import section
    code = []
    code.append(f"from {prop_enum[0]} import {prop_enum[1]}")
    code.append(f"from {item_enum[0]} import {item_enum[1]}")
    code.append("")
    code.append(f"{instance_name} = {{")
    # build mapping section
    for k, v in example.items():
        code.append(f"    {prop_enum[1]}.{k}: [")
        for i, item in enumerate(v):
            if i < len(v) - 1:
                code.append(f"        {item_enum[1]}.{item},")
            else:
                code.append(f"        {item_enum[1]}.{item}")
        code.append("    ],")
    code.append("}")

    # write to file
    with open(output_path, "w") as f:
        f.write("\n".join(code))

for schema_file in MAPPING_DIR.rglob("*.json"):
    relative_path = schema_file.relative_to(MAPPING_DIR)
    python_target = (PYTHON_OUT_MAPPING / relative_path).with_suffix(".py")

    python_target.parent.mkdir(parents=True, exist_ok=True)

    generate_code(str(schema_file), str(python_target))