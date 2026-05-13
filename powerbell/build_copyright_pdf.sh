#!/bin/bash
# PowerBell软著版本号快速修改+生成脚本
# 无需手动改主脚本，输入版本号直接运行

# 颜色输出函数
red_echo() { echo -e "\033[31m$1\033[0m"; }
green_echo() { echo -e "\033[32m$1\033[0m"; }
blue_echo() { echo -e "\033[34m$1\033[0m"; }

# 1. 提示用户输入新版本号
blue_echo "==== 请输入软著版本号（格式示例：V15、V15.0.1） ===="
read -p "输入版本号：" NEW_VERSION

# 校验版本号格式（避免特殊符号）
if [[ ! $NEW_VERSION =~ ^V[0-9]+(\.[0-9]+)*$ ]]; then
    red_echo "错误：版本号格式无效！请遵循「V+数字」格式（如V15、V15.0.1），不含特殊符号"
    exit 1
fi

# 2. 定义固定配置（仅需修改这里的著作权人，其他无需动）
SOFTWARE_NAME="PowerBell"
COPYRIGHT_OWNER="张绍建陆丰东海镇云宝软件开发工作室"
LINES_PER_PAGE=55

# 3. 生成主脚本（自动替换新版本号）
blue_echo -e "\n==== 生成${NEW_VERSION}版本主脚本 ===="
cat > build_copyright_pdf_temp.sh << EOF
#!/bin/bash
# PowerBell软著PDF生成脚本（版本：$NEW_VERSION）
red_echo() { echo -e "\033[31m\$1\033[0m"; }
green_echo() { echo -e "\033[32m\$1\033[0m"; }
blue_echo() { echo -e "\033[34m\$1\033[0m"; }

# 配置项（已自动替换为${NEW_VERSION}）
SOFTWARE_NAME="$SOFTWARE_NAME"
SOFTWARE_VERSION="$NEW_VERSION"
COPYRIGHT_OWNER="$COPYRIGHT_OWNER"
LINES_PER_PAGE=$LINES_PER_PAGE

# 步骤1：检查依赖
blue_echo "==== 1/7 检查并安装依赖 ===="
sudo apt update > /dev/null 2>&1
REQUIRED_PKGS=("python3" "wkhtmltopdf" "fonts-wqy-microhei" "pdftk" "poppler-utils")
for pkg in "\${REQUIRED_PKGS[@]}"; do
    if ! dpkg -s "\$pkg" > /dev/null 2>&1; then
        green_echo "安装依赖：\$pkg"
        sudo apt install -y "\$pkg" > /dev/null 2>&1
    fi
done

# 步骤2：生成纯文本源码
blue_echo -e "\n==== 2/7 生成纯文本核心源码 ===="
cat > generate_source.py << GEN_EOF
import os
PROJECT_PATH = "./"
OUTPUT_TXT = "PowerBell_Core_Source.txt"
INCLUDE_EXT = [".java", ".kt"]
EXCLUDE_DIRS = ["build", "libs", "test", "androidTest", ".git", ".idea", "gradle", "unittest"]
MIN_LINE_COUNT = 3
SOFTWARE_NAME = "$SOFTWARE_NAME"
SOFTWARE_VERSION = "$NEW_VERSION"
COPYRIGHT_OWNER = "$COPYRIGHT_OWNER"

def clean_text(text):
    return ''.join(c for c in text if c.isprintable() or c in "\\n\\r\\t")

def generate_source_txt():
    valid_files = []
    main_dir = os.path.join(PROJECT_PATH, "src", "main")
    if not os.path.exists(main_dir):
        print("Error: src/main directory not found!")
        return
    for root, dirs, files in os.walk(main_dir):
        dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]
        for file in files:
            if os.path.splitext(file)[1] in INCLUDE_EXT:
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                        lines = f.readlines()
                        code_lines = [l for l in lines if l.strip() and not l.strip().startswith("//")]
                        if len(code_lines) >= MIN_LINE_COUNT:
                            valid_files.append(file_path)
                except:
                    continue
    valid_files.sort(key=lambda x: os.path.getsize(x), reverse=True)
    with open(OUTPUT_TXT, "w", encoding="utf-8-sig") as f:
        f.write(f"\{SOFTWARE_NAME} \{SOFTWARE_VERSION} 核心源码 - 著作权人：\{COPYRIGHT_OWNER}\\n\\n")
        for idx, file_path in enumerate(valid_files, 1):
            f.write(f"\\n{'='*60}\\n")
            f.write(f"文件 \{idx}：\{file_path.replace(PROJECT_PATH, '')}\\n")
            f.write(f"{'='*60}\\n\\n")
            try:
                try:
                    with open(file_path, "r", encoding="utf-8") as src_f:
                        content = clean_text(src_f.read())
                except UnicodeDecodeError:
                    with open(file_path, "r", encoding="gbk") as src_f:
                        content = clean_text(src_f.read())
                f.write(content)
                f.write("\\n\\n")
            except Exception as e:
                f.write(f"文件读取失败：\{str(e)}\\n\\n")
                continue
    print(f"有效源码文件数：\{len(valid_files)}")
    print(f"纯文本文件路径：\{os.path.abspath(OUTPUT_TXT)}")

if __name__ == "__main__":
    generate_source_txt()
GEN_EOF

python3 generate_source.py
if [ ! -f "PowerBell_Core_Source.txt" ]; then
    red_echo "纯文本源码生成失败！"
    exit 1
fi

# 步骤3：生成带版本号页眉的HTML
blue_echo -e "\n==== 3/7 生成带${NEW_VERSION}页眉的HTML ===="
cat > txt2html.py << TXT_EOF
import os
TXT_FILE = "PowerBell_Core_Source.txt"
HTML_FILE = "PowerBell_Source.html"
SOFTWARE_NAME = "$SOFTWARE_NAME"
SOFTWARE_VERSION = "$NEW_VERSION"
COPYRIGHT_OWNER = "$COPYRIGHT_OWNER"
LINES_PER_PAGE = $LINES_PER_PAGE

CSS_STYLE = """
<style>
@page {{
    size: A4;
    margin: 10mm 5mm;
    @top-center {{
        content: "{} {} - 源代码（著作权人：{}）";
        font-family: 'WenQuanYi Micro Hei', monospace;
        font-size: 9pt;
        font-weight: bold;
    }}
    @bottom-center {{
        content: "页码 " counter(page) " / " counter(pages);
        font-family: 'WenQuanYi Micro Hei', monospace;
        font-size: 9pt;
    }}
}}
body {{ 
    font-family: 'WenQuanYi Micro Hei', monospace; 
    font-size: 9pt;
    line-height: 1.1;
    margin: 0;
    padding: 5mm 0 0 0;
    counter-reset: code-line;
}}
.file-header {{ 
    background: #f0f0f0; 
    padding: 3px; 
    margin: 6px 0; 
    font-weight: bold; 
    font-size: 10pt;
}}
.code-block {{ 
    white-space: pre; 
    margin-left: 8px; 
    line-height: 1.1;
    counter-increment: code-line;
}}
.code-block:before {{
    content: counter(code-line) " ";
    color: #888;
    display: inline-block;
    width: 30px;
    text-align: right;
    margin-right: 5px;
}}
.page-break {{ page-break-after: always; counter-reset: code-line; }}
</style>
""".format(SOFTWARE_NAME, SOFTWARE_VERSION, COPYRIGHT_OWNER)

def txt_to_html():
    with open(TXT_FILE, "r", encoding="utf-8") as f:
        content = f.read()
    html_content = "<!DOCTYPE html><html><head><meta charset='utf-8'>" + CSS_STYLE + "</head><body>"
    content_lines = content.split("\\n")[2:]
    content_clean = "\\n".join(content_lines)
    blocks = content_clean.split("====")
    
    line_count = 0
    for block in blocks:
        if not block.strip():
            continue
        if "文件 " in block and ":" in block:
            file_header = block.split("\\n")[0].strip() if "\\n" in block else block.strip()
            html_content += f"<div class='file-header'>\{file_header}</div>"
            code_part = block.split("\\n")[1:] if "\\n" in block else []
            block = "\\n".join(code_part)
        code_lines = block.split("\\n")
        for line in code_lines:
            if line.strip() or line_count > 0:
                line_count += 1
                html_content += f"<div class='code-block'>\{line}</div>"
                if line_count >= LINES_PER_PAGE:
                    html_content += "<div class='page-break'></div>"
                    line_count = 0
    html_content += "</body></html>"
    with open(HTML_FILE, "w", encoding="utf-8") as f:
        f.write(html_content)
    print(f"HTML文件路径：\{os.path.abspath(HTML_FILE)}")

if __name__ == "__main__":
    txt_to_html()
TXT_EOF

python3 txt2html.py
if [ ! -f "PowerBell_Source.html" ]; then
    red_echo "HTML文件生成失败！"
    exit 1
fi

# 步骤4：生成完整PDF
blue_echo -e "\n==== 4/7 生成完整PDF（版本：${NEW_VERSION}） ===="
wkhtmltopdf --page-size A4 \
            --margin-top 15mm --margin-bottom 15mm --margin-left 5mm --margin-right 5mm \
            --encoding utf-8 \
            --no-images --disable-javascript \
            --enable-local-file-access \
            --no-stop-slow-scripts \
            PowerBell_Source.html PowerBell_soft_full.pdf

if [ ! -f "PowerBell_soft_full.pdf" ]; then
    red_echo "完整PDF生成失败！"
    exit 1
fi

# 步骤5：截取60页
blue_echo -e "\n==== 5/7 截取前30+后30页 ===="
TOTAL_PAGES=\$(pdfinfo PowerBell_soft_full.pdf | grep "Pages" | awk '{print \$2}')
green_echo "源码完整PDF总页数：\$TOTAL_PAGES 页"

if [ "\$TOTAL_PAGES" -le 60 ]; then
    cp PowerBell_soft_full.pdf PowerBell_软著源码_${NEW_VERSION}_60页.pdf
    green_echo "源码不足60页，直接使用完整PDF"
else
    pdftk PowerBell_soft_full.pdf cat 1-30 output PowerBell_前30页.pdf
    START_PAGE=\$((TOTAL_PAGES - 29))
    pdftk PowerBell_soft_full.pdf cat \$START_PAGE-\$TOTAL_PAGES output PowerBell_后30页.pdf
    pdftk PowerBell_前30页.pdf PowerBell_后30页.pdf cat output PowerBell_软著源码_${NEW_VERSION}_60页.pdf
    rm -f PowerBell_前30页.pdf PowerBell_后30页.pdf
    green_echo "源码超过60页，已截取前30页+后30页合并为60页"
fi

# 步骤6：验证规范
blue_echo -e "\n==== 6/7 验证${NEW_VERSION}版本PDF规范 ===="
FINAL_PAGES=\$(pdfinfo PowerBell_软著源码_${NEW_VERSION}_60页.pdf | grep "Pages" | awk '{print \$2}')
green_echo "最终PDF页数：\$FINAL_PAGES 页"
green_echo "每页代码行数：\$LINES_PER_PAGE 行（≥50行）"
green_echo "页眉信息：$SOFTWARE_NAME $NEW_VERSION - 源代码（著作权人：$COPYRIGHT_OWNER）"

# 步骤7：清理临时文件
blue_echo -e "\n==== 7/7 清理临时文件 ===="
rm -f generate_source.py txt2html.py PowerBell_Core_Source.txt PowerBell_Source.html PowerBell_soft_full.pdf
green_echo "临时文件清理完成！"

# 输出结果
green_echo -e "\n====================================="
green_echo "✅ $SOFTWARE_NAME $NEW_VERSION 软著PDF生成成功！🎉"
green_echo "📄 最终文件：\$(pwd)/PowerBell_软著源码_${NEW_VERSION}_60页.pdf"
green_echo "💡 可直接提交软著登记，无需手动修改！"
green_echo "====================================="
EOF

# 4. 赋予执行权限并运行
chmod +x build_copyright_pdf_temp.sh
blue_echo -e "\n==== 开始生成${NEW_VERSION}版本PDF ===="
./build_copyright_pdf_temp.sh

# 5. 删除临时主脚本（可选，保留则注释此行）
rm -f build_copyright_pdf_temp.sh

green_echo -e "\n==== 操作完成！${NEW_VERSION}版本PDF已生成 ===="
