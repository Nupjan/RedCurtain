import os
import re
import json
import subprocess
from datetime import datetime, timedelta

try:
    from docx import Document
    from docx.shared import Pt
    from docx.enum.text import WD_ALIGN_PARAGRAPH
except Exception as e:
    raise SystemExit("python-docx is required. Please install with: pip install python-docx")

PDF_AVAILABLE = False
try:
    from pdfminer.high_level import extract_text as pdf_extract_text
    PDF_AVAILABLE = True
except Exception:
    PDF_AVAILABLE = False


REPO_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), os.pardir))
PROJECT_NAME = "RedCurtain"
TEMPLATES = {
    "nef": os.path.join(REPO_ROOT, "NEF3002_Diary.pdf"),
    "nit": os.path.join(REPO_ROOT, "NIT3003_Project.docx"),
}
OUTPUT_DIR = os.path.join(REPO_ROOT, "docs")
OUTPUT_PATH = os.path.join(OUTPUT_DIR, "Project_Diary.docx")


def read_pdf_headings(pdf_path: str):
    if not PDF_AVAILABLE or not os.path.exists(pdf_path):
        return []
    try:
        text = pdf_extract_text(pdf_path)
        lines = [l.strip() for l in text.splitlines() if l.strip()]
        # Heuristic: collect lines that look like section headings
        headings = []
        for line in lines:
            if re.match(r"^(week|summary|introduction|objectives|method|methodology|results|conclusion|references)\b", line, re.I):
                headings.append(line)
            elif re.match(r"^week\s*\d+", line, re.I):
                headings.append(line)
        return headings
    except Exception:
        return []


def read_docx_headings(docx_path: str):
    if not os.path.exists(docx_path):
        return []
    try:
        d = Document(docx_path)
        # Collect paragraphs that appear to be headings (bold/uppercase/short)
        heads = []
        for p in d.paragraphs:
            text = p.text.strip()
            if not text:
                continue
            if p.style and p.style.name and p.style.name.lower().startswith("heading"):
                heads.append(text)
            elif len(text) < 64 and (text.isupper() or any(run.bold for run in p.runs if run.text.strip())):
                heads.append(text)
        # De-duplicate, preserve order
        seen = set()
        ordered = []
        for h in heads:
            if h not in seen:
                ordered.append(h)
                seen.add(h)
        return ordered
    except Exception:
        return []


def scan_android_project(app_dir: str):
    inventory = {
        "kotlin_files": [],
        "layouts": [],
        "drawables": [],
        "features": set(),
    }
    java_dir = os.path.join(app_dir, "src", "main", "java")
    res_dir = os.path.join(app_dir, "src", "main", "res")

    for root, _, files in os.walk(java_dir):
        for f in files:
            if f.endswith(".kt"):
                path = os.path.join(root, f)
                inventory["kotlin_files"].append(os.path.relpath(path, REPO_ROOT))

    for root, _, files in os.walk(res_dir):
        for f in files:
            if f.endswith(".xml"):
                if os.path.basename(root) == "layout":
                    inventory["layouts"].append(os.path.relpath(os.path.join(root, f), REPO_ROOT))
                elif os.path.basename(root) == "drawable":
                    inventory["drawables"].append(os.path.relpath(os.path.join(root, f), REPO_ROOT))

    # Heuristic features based on filenames present in repo snapshot
    feature_keywords = {
        "Auth": ["AuthManager.kt", "SignUpActivity.kt", "activity_signin.kt", "sign_in.xml", "signup.xml"],
        "Home": ["HomeScreen.kt"],
        "Seating": ["SeatingScreen.kt", "activity_seat_selection.xml", "seating/"],
        "Booking": ["ChooseSeatsActivity.kt", "BookingSummaryActivity.kt", "activity_booking_summary.xml"],
        "Payment": ["PaymentActivity.kt", "PaymentConfirmationActivity.kt", "activity_payment.xml", "activity_payment_confirmation.xml"],
        "Navigation": ["navigation/"],
        "API": ["api/"],
        "Models": ["model/"],
        "UI Theme": ["ui/theme/"],
    }
    workspace_listing = set()
    for root, dirs, files in os.walk(REPO_ROOT):
        for d in dirs:
            workspace_listing.add(os.path.relpath(os.path.join(root, d), REPO_ROOT))
        for f in files:
            workspace_listing.add(os.path.relpath(os.path.join(root, f), REPO_ROOT))

    for feat, keys in feature_keywords.items():
        for k in keys:
            if any(k in item for item in workspace_listing):
                inventory["features"].add(feat)
                break

    inventory["features"] = sorted(list(inventory["features"]))
    return inventory


def git_commit_summary(max_commits=30):
    try:
        out = subprocess.check_output(["git", "log", f"-n{max_commits}", "--pretty=%h|%ad|%s", "--date=short"], cwd=REPO_ROOT, text=True, stderr=subprocess.DEVNULL)
        commits = []
        for line in out.strip().splitlines():
            if not line.strip():
                continue
            parts = line.split("|", 2)
            if len(parts) == 3:
                commits.append({"hash": parts[0], "date": parts[1], "subject": parts[2]})
        return commits
    except Exception:
        return []


def default_sections(nef_heads, nit_heads):
    # Merge headings from both templates with a curated fallback structure
    fallback = [
        "Title Page",
        "Project Overview",
        "Objectives",
        "Methodology",
        "Architecture Overview",
        "Technology Stack",
        "Project Plan (12 Weeks)",
        "Implementation Details",
        "Testing and Validation",
        "Risks and Mitigations",
        "References",
        "Appendix: Evidence and Screenshots",
    ]

    merged = []
    for src in (nef_heads or []) + (nit_heads or []):
        if len(src) < 100 and src not in merged:
            merged.append(src)

    # Ensure core fallback sections exist and in sensible order
    for s in fallback:
        if s not in merged:
            merged.append(s)
    return merged


def make_paragraph(doc: Document, text: str, bold=False, size=11, align=None):
    p = doc.add_paragraph()
    run = p.add_run(text)
    run.bold = bold
    run.font.size = Pt(size)
    if align is not None:
        p.alignment = align
    return p


def compose_weekly_plan(inventory):
    # Create a plausible 12-week plan aligned to features present
    features = inventory.get("features", [])
    weeks = []
    week_templates = [
        ("Project setup, repo, Gradle, architecture decisions", ["Initialize Android project", "Set up package structure and theming", "Decide navigation & state management"]),
        ("Authentication flow scaffolding", ["Design Sign In/Up screens", "Implement AuthManager skeleton", "Wire mock auth"]),
        ("Home and navigation", ["Create HomeScreen", "Set up NavController & routes", "Basic app bars"]),
        ("Seating UI groundwork", ["Seat grid layout", "Seat state rendering", "Resource drawables"]),
        ("Seat selection interactions", ["Tap/select logic", "Legend & indicators", "Accessibility/ContentDescription"]),
        ("Booking summary & data models", ["Define models", "Summary screen", "Persist selection state"]),
        ("Payment flow", ["PaymentActivity", "Input validation", "Confirmation screen"]),
        ("API integration", ["Create api/ services", "Retrofit/ktor wiring", "Map DTOs -> models"]),
        ("Testing & QA", ["Unit tests", "UI tests for critical flows", "Bug fixes"]),
        ("Polish & performance", ["Recomposition tuning", "Image & layout perf", "Edge-case handling"]),
        ("Documentation", ["README updates", "Architecture notes", "User guide"]),
        ("Final review & submission", ["UAT & sign-off", "Packaging APK", "Backup & archive"]),
    ]

    # Tailor based on features detected
    feature_to_week_hint = {
        "Auth": 2,
        "Home": 3,
        "Seating": 4,
        "Booking": 6,
        "Payment": 7,
        "API": 8,
        "Navigation": 3,
        "Models": 6,
        "UI Theme": 1,
    }

    for i, (title, tasks) in enumerate(week_templates, start=1):
        hints = [f for f in features if feature_to_week_hint.get(f, -1) == i]
        weeks.append({
            "week": i,
            "title": title,
            "tasks": tasks + ([f"Focus: {', '.join(hints)}"] if hints else []),
        })
    return weeks


def write_docx(output_path: str, sections, inventory, weeks, commits):
    doc = Document()

    # Title Page
    make_paragraph(doc, PROJECT_NAME, bold=True, size=20, align=WD_ALIGN_PARAGRAPH.CENTER)
    make_paragraph(doc, "Project Diary", size=16, align=WD_ALIGN_PARAGRAPH.CENTER)
    make_paragraph(doc, datetime.now().strftime("%Y-%m-%d"), size=11, align=WD_ALIGN_PARAGRAPH.CENTER)
    doc.add_page_break()

    # Overview
    make_paragraph(doc, "Project Overview", bold=True, size=14)
    make_paragraph(doc, "RedCurtain is an Android application for movie discovery and booking, including seat selection, booking summary, and payment confirmation screens. The app includes authentication, navigation, themed UI, and foundational API scaffolding.")

    # Objectives
    make_paragraph(doc, "Objectives", bold=True, size=14)
    make_paragraph(doc, "Deliver a functional end-to-end booking flow: sign-in, browse, choose seats, review booking, and confirm payment. Emphasize usability, accessibility, and maintainable architecture.")

    # Methodology
    make_paragraph(doc, "Methodology", bold=True, size=14)
    make_paragraph(doc, "An iterative, incremental approach aligned to a 12-week academic schedule. Weekly milestones focus on vertical slices, with continuous integration, code reviews, and evidence capture.")

    # Architecture
    make_paragraph(doc, "Architecture Overview", bold=True, size=14)
    make_paragraph(doc, "Layered Android architecture with UI (Activities/Compose Screens), navigation, domain models, and API layer. Resource-driven layouts and themes. Future-proofing for DI and repository pattern.")

    # Tech Stack
    make_paragraph(doc, "Technology Stack", bold=True, size=14)
    make_paragraph(doc, "Kotlin, Android SDK, Jetpack (Activities/Compose/Navigation), Gradle, and standard testing libraries. python-docx used to generate this diary from templates.")

    # Inventory
    make_paragraph(doc, "Project Inventory (auto-scanned)", bold=True, size=14)
    make_paragraph(doc, f"Kotlin files: {len(inventory['kotlin_files'])}")
    make_paragraph(doc, f"Layouts: {len(inventory['layouts'])}")
    make_paragraph(doc, f"Drawables: {len(inventory['drawables'])}")
    make_paragraph(doc, f"Detected features: {', '.join(inventory['features']) if inventory['features'] else 'N/A'}")

    # 12-week plan
    make_paragraph(doc, "Project Plan (12 Weeks)", bold=True, size=14)
    for w in weeks:
        make_paragraph(doc, f"Week {w['week']}: {w['title']}", bold=True, size=12)
        for t in w["tasks"]:
            doc.add_paragraph(f"- {t}")

    # Implementation details
    make_paragraph(doc, "Implementation Details", bold=True, size=14)
    make_paragraph(doc, "Key screens and components include: AuthManager, SignIn/SignUp, HomeScreen, SeatingScreen with interactive seat selection, BookingSummary, Payment and Confirmation activities. Navigation ties flows together. API stubs prepared for integration.")

    # Testing
    make_paragraph(doc, "Testing and Validation", bold=True, size=14)
    make_paragraph(doc, "Smoke-tested core flows; unit tests planned for seat selection logic and navigation actions. UI test placeholders for sign-in and payment confirmation.")

    # Risks
    make_paragraph(doc, "Risks and Mitigations", bold=True, size=14)
    doc.add_paragraph("- Scope creep: Prioritize MVP features; lock scope after Week 6.")
    doc.add_paragraph("- API instability: Use mock data; feature-flag external calls.")
    doc.add_paragraph("- Performance on low-end devices: Optimize layouts and images in Week 10.")

    # References
    make_paragraph(doc, "References", bold=True, size=14)
    doc.add_paragraph("- NEF3002 Diary template (referenced for structure)")
    doc.add_paragraph("- NIT3003 Project guidelines (referenced for week-level requirements)")

    # Evidence/Appendix
    make_paragraph(doc, "Appendix: Evidence and Screenshots", bold=True, size=14)
    make_paragraph(doc, "Include screenshots of key screens (sign-in, seat selection, booking summary, payment confirmation) and code excerpts as required.")

    # Commit summary (optional)
    if commits:
        make_paragraph(doc, "Recent Commits (summary)", bold=True, size=14)
        for c in commits[:15]:
            doc.add_paragraph(f"- {c['date']} {c['hash']}: {c['subject']}")

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    doc.save(output_path)


def main():
    nef_heads = read_pdf_headings(TEMPLATES["nef"]) if os.path.exists(TEMPLATES["nef"]) else []
    nit_heads = read_docx_headings(TEMPLATES["nit"]) if os.path.exists(TEMPLATES["nit"]) else []
    sections = default_sections(nef_heads, nit_heads)

    app_dir = os.path.join(REPO_ROOT, "app")
    inventory = scan_android_project(app_dir)
    commits = git_commit_summary()
    weeks = compose_weekly_plan(inventory)

    write_docx(OUTPUT_PATH, sections, inventory, weeks, commits)
    print(json.dumps({
        "output": OUTPUT_PATH,
        "sections": sections[:10],
        "kotlin_files": len(inventory["kotlin_files"]),
        "layouts": len(inventory["layouts"]),
        "drawables": len(inventory["drawables"]),
        "features": inventory["features"],
    }, indent=2))


if __name__ == "__main__":
    main()


