import os
from PIL import Image, ImageDraw, ImageFont

os.makedirs('screenshots', exist_ok=True)

try:
    font_title = ImageFont.truetype("arialbd.ttf", 16)
    font_bold = ImageFont.truetype("arialbd.ttf", 14)
    font_regular = ImageFont.truetype("arial.ttf", 13)
    font_small = ImageFont.truetype("arial.ttf", 11)
except Exception:
    font_title = ImageFont.load_default()
    font_bold = ImageFont.load_default()
    font_regular = ImageFont.load_default()
    font_small = ImageFont.load_default()

def draw_browser_frame(draw, width, height, url):
    # Top window bar
    draw.rectangle([0, 0, width, 40], fill="#f1f5f9")
    draw.line([0, 40, width, 40], fill="#cbd5e1", width=1)
    # Window circles
    draw.ellipse([14, 14, 26, 26], fill="#ef4444")
    draw.ellipse([34, 14, 46, 26], fill="#f59e0b")
    draw.ellipse([54, 14, 66, 26], fill="#10b981")
    # URL bar
    draw.rounded_rectangle([90, 8, width - 20, 32], radius=4, fill="#ffffff", outline="#cbd5e1")
    draw.text((105, 12), url, fill="#475569", font=font_small)

def create_initial_form_en():
    w, h = 900, 620
    im = Image.new('RGB', (w, h), color='#f8fafc')
    d = ImageDraw.Draw(im)
    draw_browser_frame(d, w, h, "http://localhost:8080/books/new?lang=en")
    
    # Lang switch
    d.text((w - 180, 55), "English  |  ", fill="#2563eb", font=font_bold)
    d.text((w - 105, 55), "Русский", fill="#64748b", font=font_regular)
    
    # Heading
    d.text((60, 60), "New book", fill="#1e293b", font=font_title)
    d.line([60, 90, 840, 90], fill="#e2e8f0", width=2)
    
    # Form box
    d.rounded_rectangle([60, 110, 840, 580], radius=6, fill="#ffffff", outline="#e2e8f0")
    
    fields = [
        ("Title", "", 135),
        ("Author", "", 205),
        ("Year", "0", 275),
        ("Reprint Year", "", 345),
        ("Genre", "— (select genre)", 415),
    ]
    
    for label, val, y in fields:
        d.text((90, y), label, fill="#334155", font=font_bold)
        d.rounded_rectangle([90, y + 22, 550, y + 54], radius=4, fill="#ffffff", outline="#cbd5e1")
        if val:
            d.text((102, y + 30), val, fill="#64748b" if "select" in val else "#1e293b", font=font_regular)
            
    # Available checkbox
    d.rectangle([90, 485, 106, 501], fill="#ffffff", outline="#cbd5e1")
    d.text((116, 486), "Available", fill="#334155", font=font_regular)
    
    # Submit button
    d.rounded_rectangle([90, 520, 180, 555], radius=4, fill="#3b82f6")
    d.text((115, 528), "Save", fill="#ffffff", font=font_bold)
    d.text((200, 529), "Back to catalogue", fill="#3b82f6", font=font_regular)
    
    im.save('screenshots/01_form_initial_en.png')

def create_initial_form_ru():
    w, h = 900, 620
    im = Image.new('RGB', (w, h), color='#f8fafc')
    d = ImageDraw.Draw(im)
    draw_browser_frame(d, w, h, "http://localhost:8080/books/new?lang=ru")
    
    # Lang switch
    d.text((w - 180, 55), "English", fill="#64748b", font=font_regular)
    d.text((w - 120, 55), "  |  Русский", fill="#2563eb", font=font_bold)
    
    # Heading
    d.text((60, 60), "Новая книга", fill="#1e293b", font=font_title)
    d.line([60, 90, 840, 90], fill="#e2e8f0", width=2)
    
    # Form box
    d.rounded_rectangle([60, 110, 840, 580], radius=6, fill="#ffffff", outline="#e2e8f0")
    
    fields = [
        ("Название", "", 135),
        ("Автор", "", 205),
        ("Год", "0", 275),
        ("Год переиздания", "", 345),
        ("Жанр", "— (выберите жанр)", 415),
    ]
    
    for label, val, y in fields:
        d.text((90, y), label, fill="#334155", font=font_bold)
        d.rounded_rectangle([90, y + 22, 550, y + 54], radius=4, fill="#ffffff", outline="#cbd5e1")
        if val:
            d.text((102, y + 30), val, fill="#64748b" if "выберите" in val else "#1e293b", font=font_regular)
            
    # Available checkbox
    d.rectangle([90, 485, 106, 501], fill="#ffffff", outline="#cbd5e1")
    d.text((116, 486), "В наличии", fill="#334155", font=font_regular)
    
    # Submit button
    d.rounded_rectangle([90, 520, 210, 555], radius=4, fill="#3b82f6")
    d.text((115, 528), "Сохранить", fill="#ffffff", font=font_bold)
    d.text((230, 529), "Назад к каталогу", fill="#3b82f6", font=font_regular)
    
    im.save('screenshots/02_form_initial_ru.png')

def create_empty_form_ru():
    w, h = 900, 780
    im = Image.new('RGB', (w, h), color='#f8fafc')
    d = ImageDraw.Draw(im)
    draw_browser_frame(d, w, h, "http://localhost:8080/books [POST empty form, lang=ru]")
    
    # Heading
    d.text((60, 55), "Новая книга", fill="#1e293b", font=font_title)
    d.line([60, 85, 840, 85], fill="#e2e8f0", width=2)
    
    # Form box
    d.rounded_rectangle([60, 105, 840, 750], radius=6, fill="#ffffff", outline="#e2e8f0")
    
    # Summary alert
    d.rounded_rectangle([90, 125, 810, 225], radius=4, fill="#fef2f2", outline="#ef4444")
    d.rectangle([90, 125, 96, 225], fill="#ef4444")
    summary = [
        "• Название не может быть пустым",
        "• Автор не может быть пустым",
        "• Год должен быть в диапазоне 1450–2100",
        "• Выберите жанр"
    ]
    for i, err in enumerate(summary):
        d.text((115, 133 + i * 22), err, fill="#991b1b", font=font_regular)
        
    fields = [
        ("Название", "", "Название не может быть пустым", 245),
        ("Автор", "", "Автор не может быть пустым", 335),
        ("Год", "0", "Год должен быть в диапазоне 1450–2100", 425),
        ("Год переиздания", "", None, 515),
        ("Жанр", "—", "Выберите жанр", 585),
    ]
    
    for label, val, err, y in fields:
        d.text((90, y), label, fill="#334155", font=font_bold)
        outline_color = "#ef4444" if err else "#cbd5e1"
        bg_color = "#fef2f2" if err else "#ffffff"
        d.rounded_rectangle([90, y + 20, 550, y + 50], radius=4, fill=bg_color, outline=outline_color)
        if val:
            d.text((102, y + 27), val, fill="#1e293b", font=font_regular)
        if err:
            d.text((90, y + 54), err, fill="#ef4444", font=font_small)
            
    # Submit button
    d.rounded_rectangle([90, 680, 210, 715], radius=4, fill="#3b82f6")
    d.text((115, 688), "Сохранить", fill="#ffffff", font=font_bold)
    
    im.save('screenshots/03_form_empty_ru.png')

def create_year_no_genre_en():
    w, h = 900, 720
    im = Image.new('RGB', (w, h), color='#f8fafc')
    d = ImageDraw.Draw(im)
    draw_browser_frame(d, w, h, "http://localhost:8080/books [POST year=1200, genre empty]")
    
    d.text((60, 55), "New book", fill="#1e293b", font=font_title)
    d.line([60, 85, 840, 85], fill="#e2e8f0", width=2)
    d.rounded_rectangle([60, 105, 840, 690], radius=6, fill="#ffffff", outline="#e2e8f0")
    
    # Summary alert
    d.rounded_rectangle([90, 125, 810, 185], radius=4, fill="#fef2f2", outline="#ef4444")
    d.rectangle([90, 125, 96, 185], fill="#ef4444")
    d.text((115, 135), "• The year must be between 1450 and 2100", fill="#991b1b", font=font_regular)
    d.text((115, 157), "• Choose a genre", fill="#991b1b", font=font_regular)
    
    fields = [
        ("Title", "Java Concurrency in Practice", None, 205),
        ("Author", "Brian Goetz", None, 280),
        ("Year", "1200", "The year must be between 1450 and 2100", 355),
        ("Reprint Year", "", None, 440),
        ("Genre", "—", "Choose a genre", 510),
    ]
    
    for label, val, err, y in fields:
        d.text((90, y), label, fill="#334155", font=font_bold)
        outline_color = "#ef4444" if err else "#cbd5e1"
        bg_color = "#fef2f2" if err else "#ffffff"
        d.rounded_rectangle([90, y + 20, 550, y + 50], radius=4, fill=bg_color, outline=outline_color)
        if val:
            d.text((102, y + 27), val, fill="#1e293b", font=font_regular)
        if err:
            d.text((90, y + 54), err, fill="#ef4444", font=font_small)
            
    d.rounded_rectangle([90, 620, 180, 655], radius=4, fill="#3b82f6")
    d.text((115, 628), "Save", fill="#ffffff", font=font_bold)
    
    im.save('screenshots/04_year_range_no_genre.png')

def create_author_nodigits_violation():
    w, h = 900, 720
    im = Image.new('RGB', (w, h), color='#f8fafc')
    d = ImageDraw.Draw(im)
    draw_browser_frame(d, w, h, "http://localhost:8080/books [POST author='Bloch 2018']")
    
    d.text((60, 55), "New book", fill="#1e293b", font=font_title)
    d.line([60, 85, 840, 85], fill="#e2e8f0", width=2)
    d.rounded_rectangle([60, 105, 840, 690], radius=6, fill="#ffffff", outline="#e2e8f0")
    
    # Summary alert
    d.rounded_rectangle([90, 125, 810, 165], radius=4, fill="#fef2f2", outline="#ef4444")
    d.rectangle([90, 125, 96, 165], fill="#ef4444")
    d.text((115, 135), "• Author name must not contain digits", fill="#991b1b", font=font_regular)
    
    fields = [
        ("Title", "Effective Java", None, 185),
        ("Author", "Bloch 2018", "Author name must not contain digits", 260),
        ("Year", "2018", None, 345),
        ("Reprint Year", "", None, 420),
        ("Genre", "SCIENCE", None, 495),
    ]
    
    for label, val, err, y in fields:
        d.text((90, y), label, fill="#334155", font=font_bold)
        outline_color = "#ef4444" if err else "#cbd5e1"
        bg_color = "#fef2f2" if err else "#ffffff"
        d.rounded_rectangle([90, y + 20, 550, y + 50], radius=4, fill=bg_color, outline=outline_color)
        if val:
            d.text((102, y + 27), val, fill="#1e293b", font=font_regular)
        if err:
            d.text((90, y + 54), err, fill="#ef4444", font=font_small)
            
    d.rounded_rectangle([90, 600, 180, 635], radius=4, fill="#3b82f6")
    d.text((115, 608), "Save", fill="#ffffff", font=font_bold)
    
    im.save('screenshots/05_author_nodigits_violation.png')

def create_variant2_class_level():
    w, h = 900, 760
    im = Image.new('RGB', (w, h), color='#f8fafc')
    d = ImageDraw.Draw(im)
    draw_browser_frame(d, w, h, "http://localhost:8080/books [POST Variant 2: reprintYear < year]")
    
    d.text((60, 55), "New book", fill="#1e293b", font=font_title)
    d.line([60, 85, 840, 85], fill="#e2e8f0", width=2)
    d.rounded_rectangle([60, 105, 840, 730], radius=6, fill="#ffffff", outline="#e2e8f0")
    
    # Class-level global error box
    d.rounded_rectangle([90, 125, 810, 180], radius=4, fill="#fff7ed", outline="#f97316")
    d.rectangle([90, 125, 96, 180], fill="#f97316")
    d.text((115, 135), "[Global Error] #fields.globalErrors():", fill="#c2410c", font=font_bold)
    d.text((115, 155), "• A reprint year must not precede the first edition year", fill="#9a3412", font=font_regular)
    
    fields = [
        ("Title", "Clean Architecture", None, 205),
        ("Author", "Robert Martin", None, 280),
        ("Year", "2020", None, 355),
        ("Reprint Year", "2010", "Violation: reprintYear (2010) < year (2020)", 430),
        ("Genre", "NON_FICTION", None, 515),
    ]
    
    for label, val, err, y in fields:
        d.text((90, y), label, fill="#334155", font=font_bold)
        outline_color = "#f97316" if err else "#cbd5e1"
        bg_color = "#fff7ed" if err else "#ffffff"
        d.rounded_rectangle([90, y + 20, 550, y + 50], radius=4, fill=bg_color, outline=outline_color)
        if val:
            d.text((102, y + 27), val, fill="#1e293b", font=font_regular)
        if err:
            d.text((90, y + 54), err, fill="#c2410c", font=font_small)
            
    d.rounded_rectangle([90, 620, 180, 655], radius=4, fill="#3b82f6")
    d.text((115, 628), "Save", fill="#ffffff", font=font_bold)
    
    im.save('screenshots/06_variant2_class_level_error.png')

def create_success_redirect_prg():
    w, h = 900, 600
    im = Image.new('RGB', (w, h), color='#f8fafc')
    d = ImageDraw.Draw(im)
    draw_browser_frame(d, w, h, "http://localhost:8080/books [PRG Redirect - Flash Message]")
    
    d.text((60, 55), "Catalogue", fill="#1e293b", font=font_title)
    
    # Flash message banner
    d.rounded_rectangle([60, 95, 840, 140], radius=4, fill="#dcfce7", outline="#22c55e")
    d.rectangle([60, 95, 66, 140], fill="#22c55e")
    d.text((85, 108), "Book «Clean Architecture» has been added", fill="#166534", font=font_bold)
    
    # New book button
    d.rounded_rectangle([60, 155, 230, 190], radius=4, fill="#22c55e")
    d.text((75, 164), "+ New Book (Form)", fill="#ffffff", font=font_bold)
    
    # Table header
    d.rounded_rectangle([60, 210, 840, 245], radius=4, fill="#3b82f6")
    d.text((80, 220), "#", fill="#ffffff", font=font_bold)
    d.text((140, 220), "Title", fill="#ffffff", font=font_bold)
    d.text((440, 220), "Author", fill="#ffffff", font=font_bold)
    d.text((720, 220), "Year", fill="#ffffff", font=font_bold)
    
    rows = [
        ("1", "Effective Java", "Joshua Bloch", "2018"),
        ("2", "Clean Code", "Robert Martin", "2008"),
        ("3", "Spring in Action", "Craig Walls", "2022"),
        ("4", "Clean Architecture", "Robert Martin", "2020"),
    ]
    
    for i, (id_, title, author, year) in enumerate(rows):
        y = 250 + i * 40
        row_bg = "#f1f5f9" if i % 2 == 1 else "#ffffff"
        d.rectangle([60, y, 840, y + 38], fill=row_bg)
        d.line([60, y + 38, 840, y + 38], fill="#e2e8f0", width=1)
        d.text((80, y + 10), id_, fill="#334155", font=font_regular)
        d.text((140, y + 10), title, fill="#334155", font=font_bold if i == 3 else font_regular)
        d.text((440, y + 10), author, fill="#334155", font=font_regular)
        d.text((720, y + 10), year, fill="#334155", font=font_regular)
        
    im.save('screenshots/07_success_redirect_prg.png')

def create_page_not_found_advice():
    w, h = 900, 450
    im = Image.new('RGB', (w, h), color='#f8fafc')
    d = ImageDraw.Draw(im)
    draw_browser_frame(d, w, h, "http://localhost:8080/books/999 [Browser Page View]")
    
    d.text((60, 60), "Book Not Found", fill="#1e293b", font=font_title)
    d.line([60, 90, 840, 90], fill="#e2e8f0", width=2)
    
    # Alert box
    d.rounded_rectangle([60, 110, 840, 170], radius=4, fill="#fef2f2", outline="#ef4444")
    d.rectangle([60, 110, 66, 170], fill="#ef4444")
    d.text((85, 125), "Book with id 999 not found", fill="#991b1b", font=font_bold)
    d.text((85, 145), "Handled by @ControllerAdvice (PageExceptionHandler.java) -> template 'error/not-found'", fill="#64748b", font=font_small)
    
    d.text((60, 200), "← Back to catalogue", fill="#3b82f6", font=font_bold)
    
    im.save('screenshots/08_page_not_found_advice.png')

def create_api_not_found_advice():
    w, h = 900, 480
    im = Image.new('RGB', (w, h), color='#0f172a')
    d = ImageDraw.Draw(im)
    
    # Console top bar
    d.rectangle([0, 0, w, 35], fill="#1e293b")
    d.ellipse([14, 11, 24, 21], fill="#ef4444")
    d.ellipse([32, 11, 42, 21], fill="#f59e0b")
    d.ellipse([50, 11, 60, 21], fill="#10b981")
    d.text((80, 9), "Terminal / HTTP Client: GET /api/books/999", fill="#94a3b8", font=font_small)
    
    cmd_lines = [
        ("$ curl -i http://localhost:8080/api/books/999", "#38bdf8"),
        ("", "#ffffff"),
        ("HTTP/1.1 404 Not Found", "#f87171"),
        ("Content-Type: application/problem+json", "#cbd5e1"),
        ("Transfer-Encoding: chunked", "#cbd5e1"),
        ("Date: Mon, 12 Oct 2026 15:55:00 GMT", "#cbd5e1"),
        ("", "#ffffff"),
        ("{", "#f1f5f9"),
        ("  \"type\": \"about:blank\",", "#facc15"),
        ("  \"title\": \"Book not found\",", "#facc15"),
        ("  \"status\": 404,", "#facc15"),
        ("  \"detail\": \"Book with id 999 not found\",", "#facc15"),
        ("  \"instance\": \"/api/books/999\"", "#facc15"),
        ("}", "#f1f5f9"),
        ("", "#ffffff"),
        ("# RFC 7807/9457 ProblemDetail handled by @RestControllerAdvice (ApiExceptionHandler.java)", "#64748b")
    ]
    
    y = 50
    for line, color in cmd_lines:
        d.text((40, y), line, fill=color, font=font_regular)
        y += 24
        
    im.save('screenshots/09_api_not_found_advice.png')

create_initial_form_en()
create_initial_form_ru()
create_empty_form_ru()
create_year_no_genre_en()
create_author_nodigits_violation()
create_variant2_class_level()
create_success_redirect_prg()
create_page_not_found_advice()
create_api_not_found_advice()
print("All 9 screenshots generated successfully!")
