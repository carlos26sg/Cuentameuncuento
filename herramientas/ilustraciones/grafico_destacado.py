"""Gráfico destacado de Google Play (1024x500): paisaje de la app, título y tres portadas.

Uso (desde la raíz del proyecto): python herramientas/ilustraciones/grafico_destacado.py
Necesita resvg-py. Escribe docs/tienda/grafico_destacado.png.
"""
import base64
from pathlib import Path

import resvg_py

RAIZ = Path(__file__).resolve().parents[2]
CUENTOS = RAIZ / "contenido" / "cuentos"
FUENTE = RAIZ / "app" / "src" / "main" / "res" / "font" / "fredoka.ttf"
SALIDA = RAIZ / "docs" / "tienda" / "grafico_destacado.png"


def portada(cid):
    ruta = next(p for p in (CUENTOS / cid).glob("portada.*"))
    tipo = "jpeg" if ruta.suffix in (".jpg", ".jpeg") else ruta.suffix[1:]
    return f"data:image/{tipo};base64," + base64.b64encode(ruta.read_bytes()).decode()


def tarjeta(x, y, lado, giro, cid, n):
    return (f'<g transform="translate({x} {y}) rotate({giro})">'
            f'<rect x="{-lado/2+4}" y="{-lado/2+8}" width="{lado}" height="{lado}" rx="28" fill="#2A2240" opacity="0.18"/>'
            f'<rect x="{-lado/2-8}" y="{-lado/2-8}" width="{lado+16}" height="{lado+16}" rx="34" fill="#FFFFFF"/>'
            f'<clipPath id="c{n}"><rect x="{-lado/2}" y="{-lado/2}" width="{lado}" height="{lado}" rx="26"/></clipPath>'
            f'<image href="{portada(cid)}" x="{-lado/2}" y="{-lado/2}" width="{lado}" height="{lado}" '
            f'preserveAspectRatio="xMidYMid slice" clip-path="url(#c{n})"/></g>')


W, H = 1024, 500
svg = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}">
<rect width="{W}" height="{H}" fill="#CBEAF5"/>
<circle cx="880" cy="95" r="58" fill="#FFD36B"/>
<g fill="#FFFFFF"><ellipse cx="150" cy="80" rx="70" ry="24"/><ellipse cx="185" cy="64" rx="44" ry="28"/>
<ellipse cx="560" cy="60" rx="56" ry="20"/><ellipse cx="588" cy="46" rx="36" ry="22"/></g>
<path d="M0 {H*.70} C{W*.18} {H*.56} {W*.36} {H*.60} {W*.54} {H*.68} C{W*.72} {H*.76} {W*.87} {H*.62} {W} {H*.60} L{W} {H} L0 {H} Z" fill="#9ED39B"/>
<path d="M0 {H*.82} C{W*.23} {H*.70} {W*.44} {H*.78} {W*.64} {H*.82} C{W*.80} {H*.86} {W*.92} {H*.76} {W} {H*.78} L{W} {H} L0 {H} Z" fill="#6DBB7A"/>
<path d="M0 {H*.95} C{W*.31} {H*.90} {W*.67} {H*.97} {W} {H*.92} L{W} {H} L0 {H} Z" fill="#FFF6E9"/>
<text x="64" y="205" font-family="Fredoka" font-weight="600" font-size="78" fill="#2A2240">Cuéntame</text>
<text x="64" y="290" font-family="Fredoka" font-weight="600" font-size="78" fill="#2A2240">un cuento</text>
{tarjeta(600, 300, 170, -7, "los_tres_cerditos", 1)}
{tarjeta(900, 315, 170, 7, "lara_la_mariquita", 2)}
{tarjeta(752, 250, 190, 0, "la_abeja_y_zeus", 3)}
</svg>"""

SALIDA.write_bytes(bytes(resvg_py.svg_to_bytes(svg_string=svg, font_files=[str(FUENTE)], skip_system_fonts=True)))
print(SALIDA, "ok")
