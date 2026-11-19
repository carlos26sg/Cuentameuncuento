"""Ilustraciones de «La abeja y Zeus»: genera img_1..img_7 y la portada en SVG y PNG.

Uso: python la_abeja_y_zeus.py <carpeta_salida>  (necesita resvg-py: pip install resvg-py)

Estilo plano con los colores de la app. Cada personaje es una función que devuelve SVG,
para que la abeja, Zeus y la colmena sean idénticos en todas las escenas.
"""
import sys
from pathlib import Path

import resvg_py

SALIDA = Path(sys.argv[1])
SALIDA.mkdir(parents=True, exist_ok=True)

TINTA = "#2A2240"
SOL = "#FFD36B"
ORO = "#E9A93A"
PIEL = "#F2C29B"
MEJILLA = "#F59C8E"
CORAL = "#EE6A4A"
LEJANA = "#9ED39B"
CERCANA = "#6DBB7A"
FONDO = "#FFF6E9"


def lienzo(ancho, alto, contenido, defs=""):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {ancho} {alto}" '
            f'width="{ancho}" height="{alto}"><defs>{DEFS}{defs}</defs>{contenido}</svg>')


DEFS = f"""
<linearGradient id="cielo" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#BFE3F2"/><stop offset="1" stop-color="#EAF6FB"/></linearGradient>
<linearGradient id="atardecer" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#FFC9A0"/><stop offset="1" stop-color="#FFEFD2"/></linearGradient>
<linearGradient id="tormenta" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#A9B6D6"/><stop offset="1" stop-color="#DCE3F0"/></linearGradient>
<radialGradient id="luz" cx="0.5" cy="0.5" r="0.5"><stop offset="0" stop-color="#FFE9A8" stop-opacity="0.95"/><stop offset="1" stop-color="#FFE9A8" stop-opacity="0"/></radialGradient>
<linearGradient id="miel" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#FFC94D"/><stop offset="1" stop-color="#E9972A"/></linearGradient>
"""


def nube(x, y, e=1.0, opacidad=0.9):
    return (f'<g transform="translate({x} {y}) scale({e})" fill="#FFFFFF" opacity="{opacidad}">'
            '<ellipse cx="0" cy="0" rx="110" ry="40"/><ellipse cx="60" cy="-25" rx="70" ry="45"/>'
            '<ellipse cx="-50" cy="-12" rx="50" ry="34"/></g>')


def colinas(ancho, alto, lejana=LEJANA, cercana=CERCANA, suelo=FONDO):
    w, h = ancho, alto
    return (f'<path d="M0 {h*.70} C{w*.18} {h*.56} {w*.36} {h*.60} {w*.54} {h*.68} '
            f'C{w*.72} {h*.76} {w*.87} {h*.62} {w} {h*.60} L{w} {h} L0 {h} Z" fill="{lejana}"/>'
            f'<path d="M0 {h*.82} C{w*.23} {h*.70} {w*.44} {h*.78} {w*.64} {h*.82} '
            f'C{w*.80} {h*.86} {w*.92} {h*.76} {w} {h*.78} L{w} {h} L0 {h} Z" fill="{cercana}"/>'
            f'<path d="M0 {h*.95} C{w*.31} {h*.90} {w*.67} {h*.97} {w} {h*.92} L{w} {h} L0 {h} Z" fill="{suelo}"/>')


def flor(x, y, color, e=1.0):
    return (f'<g transform="translate({x} {y}) scale({e})">'
            f'<path d="M0 0 Q4 40 0 80" stroke="{CERCANA}" stroke-width="7" fill="none"/>'
            f'<g fill="{color}"><circle cx="0" cy="-16" r="13"/><circle cx="15" cy="-4" r="13"/>'
            f'<circle cx="10" cy="13" r="13"/><circle cx="-10" cy="13" r="13"/><circle cx="-15" cy="-4" r="13"/></g>'
            f'<circle cx="0" cy="0" r="10" fill="{SOL}"/></g>')


def tomillo(x, y, e=1.0):
    return (f'<g transform="translate({x} {y}) scale({e})">'
            f'<path d="M0 0 L0 -60 M0 -30 L-18 -50 M0 -20 L16 -42" stroke="{CERCANA}" stroke-width="6" fill="none" stroke-linecap="round"/>'
            '<g fill="#B48AD8"><circle cx="0" cy="-66" r="9"/><circle cx="-20" cy="-55" r="8"/><circle cx="18" cy="-47" r="8"/>'
            '<circle cx="-8" cy="-74" r="7"/><circle cx="8" cy="-72" r="7"/></g></g>')


def abeja(x, y, e=1.0, mira="izquierda", gesto="alegre", corona=True, giro=0, aguijon_brillo=False):
    """Abeja con la cabeza a la izquierda; mira="derecha" la voltea."""
    sx = -e if mira == "derecha" else e
    if gesto == "alegre":
        cara = (f'<circle cx="-66" cy="-10" r="7" fill="#FFFFFF"/>'
                f'<path d="M-76 8 Q-68 16 -58 10" fill="none" stroke="#FFFFFF" stroke-width="4" stroke-linecap="round"/>')
    elif gesto == "seria":
        cara = (f'<circle cx="-66" cy="-8" r="7" fill="#FFFFFF"/>'
                f'<path d="M-78 -22 L-56 -18" stroke="#FFFFFF" stroke-width="5" stroke-linecap="round"/>'
                f'<path d="M-76 12 L-60 12" stroke="#FFFFFF" stroke-width="4" stroke-linecap="round"/>')
    elif gesto == "triste":
        cara = (f'<circle cx="-66" cy="-8" r="7" fill="#FFFFFF"/>'
                f'<path d="M-78 -18 L-58 -24" stroke="#FFFFFF" stroke-width="5" stroke-linecap="round"/>'
                f'<path d="M-76 14 Q-68 6 -58 12" fill="none" stroke="#FFFFFF" stroke-width="4" stroke-linecap="round"/>')
    else:  # pensativa
        cara = (f'<path d="M-72 -10 q6 6 12 0" fill="none" stroke="#FFFFFF" stroke-width="4" stroke-linecap="round"/>'
                f'<path d="M-74 12 L-60 10" stroke="#FFFFFF" stroke-width="4" stroke-linecap="round"/>')
    coronita = ('<path d="M-74 -30 L-70 -48 L-62 -38 L-56 -52 L-50 -38 L-44 -48 L-42 -30 Z" '
                f'fill="{SOL}" stroke="{ORO}" stroke-width="3" stroke-linejoin="round"/>') if corona else ""
    brillo = ('<g fill="#FFFFFF"><path d="M84 6 l6 14 14 6 -14 6 -6 14 -6 -14 -14 -6 14 -6 Z"/></g>'
              '<circle cx="74" cy="9" r="22" fill="#FFF3B0" opacity="0.6"/>') if aguijon_brillo else ""
    return (f'<g transform="translate({x} {y}) scale({sx} {e}) rotate({giro})">'
            '<ellipse cx="-14" cy="-40" rx="34" ry="22" fill="#FFFFFF" opacity="0.88" stroke="#BFE3F2" stroke-width="4" transform="rotate(-25 -14 -40)"/>'
            '<ellipse cx="20" cy="-44" rx="30" ry="20" fill="#FFFFFF" opacity="0.88" stroke="#BFE3F2" stroke-width="4" transform="rotate(20 20 -44)"/>'
            f'{brillo}'
            f'<ellipse cx="0" cy="0" rx="58" ry="40" fill="{SOL}"/>'
            f'<path d="M-20 -38 Q-28 0 -20 38" fill="none" stroke="{TINTA}" stroke-width="14"/>'
            f'<path d="M10 -40 Q2 0 10 40" fill="none" stroke="{TINTA}" stroke-width="14"/>'
            f'<path d="M58 0 L74 6 L58 12 Z" fill="{TINTA}"/>'
            f'<circle cx="-56" cy="-4" r="30" fill="{TINTA}"/>{cara}'
            f'<path d="M-62 -32 Q-72 -56 -86 -60" fill="none" stroke="{TINTA}" stroke-width="4" stroke-linecap="round"/>'
            f'<circle cx="-88" cy="-61" r="6" fill="{TINTA}"/>'
            f'<path d="M-50 -30 Q-46 -58 -34 -64" fill="none" stroke="{TINTA}" stroke-width="4" stroke-linecap="round"/>'
            f'<circle cx="-32" cy="-66" r="6" fill="{TINTA}"/>{coronita}</g>')


def obrera(x, y, e=0.45, mira="izquierda", giro=0):
    return abeja(x, y, e, mira, "alegre", corona=False, giro=giro)


def colmena(x, y, e=1.0):
    bandas = "".join(
        f'<ellipse cx="0" cy="{-i*42}" rx="{150 - i*22}" ry="32" fill="{"#F4B53F" if i % 2 == 0 else "#E9A23A"}"/>'
        for i in range(6))
    return (f'<g transform="translate({x} {y}) scale({e})">'
            f'<rect x="-150" y="0" width="300" height="26" rx="10" fill="#B07A4A"/>'
            f'<rect x="-120" y="20" width="22" height="120" fill="#8E5E38"/><rect x="98" y="20" width="22" height="120" fill="#8E5E38"/>'
            f'<path d="M-150 0 Q-160 -200 0 -250 Q160 -200 150 0 Z" fill="#F4B53F"/>{bandas}'
            f'<path d="M-46 0 Q-46 -60 0 -60 Q46 -60 46 0 Z" fill="{TINTA}"/>'
            f'<path d="M40 -8 Q40 30 52 44 Q64 30 64 -8 Z" fill="#FFC94D"/><circle cx="52" cy="44" r="12" fill="#FFC94D"/></g>')


def tarro(x, y, e=1.0):
    return (f'<g transform="translate({x} {y}) scale({e})">'
            '<rect x="-56" y="0" width="112" height="100" rx="26" fill="url(#miel)"/>'
            '<rect x="-62" y="-16" width="124" height="26" rx="10" fill="#C77B2E"/>'
            '<path d="M-62 -4 L62 -4" stroke="#F7F1E6" stroke-width="6" stroke-dasharray="12 10"/>'
            '<path d="M-30 8 Q-30 36 -22 44 Q-14 36 -14 8" fill="#FFC94D"/>'
            '<ellipse cx="-24" cy="40" rx="10" ry="12" fill="#FFC94D"/>'
            '<ellipse cx="22" cy="50" rx="14" ry="26" fill="#FFFFFF" opacity="0.35"/></g>')


def zeus(x, y, e=1.0, gesto="alegre", copa=False, rayo=False):
    """Zeus con el centro de la cara en (x, y)."""
    if gesto == "alegre":
        cara = (f'<path d="M-30 5 q9 -10 18 0" fill="none" stroke="{TINTA}" stroke-width="5" stroke-linecap="round"/>'
                f'<path d="M12 5 q9 -10 18 0" fill="none" stroke="{TINTA}" stroke-width="5" stroke-linecap="round"/>'
                f'<path d="M-22 45 Q0 65 22 45 Q0 55 -22 45 Z" fill="{TINTA}"/>')
        cejas = ('<path d="M-40 -10 q-8 -12 26 -12" fill="none" stroke="#FFFFFF" stroke-width="12" stroke-linecap="round"/>'
                 '<path d="M40 -10 q8 -12 -26 -12" fill="none" stroke="#FFFFFF" stroke-width="12" stroke-linecap="round"/>')
    else:  # ceño
        cara = (f'<circle cx="-22" cy="6" r="7" fill="{TINTA}"/><circle cx="22" cy="6" r="7" fill="{TINTA}"/>'
                f'<path d="M-20 52 Q0 40 20 52" fill="none" stroke="{TINTA}" stroke-width="6" stroke-linecap="round"/>')
        cejas = ('<path d="M-44 -16 L-10 -4" stroke="#FFFFFF" stroke-width="13" stroke-linecap="round"/>'
                 '<path d="M44 -16 L10 -4" stroke="#FFFFFF" stroke-width="13" stroke-linecap="round"/>')
    brazo = ""
    if copa:
        brazo = (f'<path d="M85 115 Q150 115 160 45" fill="none" stroke="#5B7BD5" stroke-width="40" stroke-linecap="round"/>'
                 f'<circle cx="162" cy="37" r="22" fill="{PIEL}"/>'
                 f'<path d="M140 -25 L184 -25 L176 15 Q162 25 148 15 Z" fill="{SOL}" stroke="{ORO}" stroke-width="4"/>'
                 f'<rect x="158" y="15" width="8" height="22" fill="{ORO}"/>')
    if rayo:
        brazo = (f'<path d="M85 115 Q150 115 160 45" fill="none" stroke="#5B7BD5" stroke-width="40" stroke-linecap="round"/>'
                 f'<circle cx="162" cy="37" r="22" fill="{PIEL}"/>'
                 f'<path d="M168 -90 L132 -10 L160 -10 L138 60 L196 -30 L166 -30 L190 -90 Z" fill="{SOL}" stroke="{ORO}" stroke-width="5" stroke-linejoin="round"/>')
    hojas = "".join(
        f'<ellipse cx="{cx}" cy="{cy}" rx="10" ry="20" transform="rotate({r} {cx} {cy})"/>'
        for cx, cy, r in ((-56, -37, -50), (-36, -55, -30), (-12, -63, -10), (12, -63, 10), (36, -55, 30), (56, -37, 50)))
    return (f'<g transform="translate({x} {y}) scale({e})">'
            '<path d="M-120 305 L-90 95 Q0 50 90 95 L120 305 Z" fill="#5B7BD5"/>'
            '<path d="M-90 95 Q-20 115 40 285" fill="none" stroke="#FFFFFF" stroke-width="22" opacity="0.9"/>'
            f'{brazo}'
            '<circle cx="0" cy="-10" r="74" fill="#FFFFFF"/>'
            f'<circle cx="0" cy="0" r="62" fill="{PIEL}"/>'
            '<path d="M-62 5 Q-70 -55 0 -63 Q70 -55 62 5 Q50 -35 0 -37 Q-50 -35 -62 5 Z" fill="#FFFFFF"/>'
            '<path d="M-62 10 Q-70 115 0 135 Q70 115 62 10 Q40 55 0 55 Q-40 55 -62 10 Z" fill="#FFFFFF"/>'
            '<g fill="#FFFFFF"><circle cx="-38" cy="110" r="22"/><circle cx="0" cy="125" r="24"/><circle cx="38" cy="110" r="22"/></g>'
            f'<g fill="{CERCANA}">{hojas}</g>{cejas}{cara}'
            f'<circle cx="-38" cy="27" r="10" fill="{MEJILLA}" opacity="0.6"/><circle cx="38" cy="27" r="10" fill="{MEJILLA}" opacity="0.6"/>'
            '</g>')


def hera(x, y, e=1.0):
    return (f'<g transform="translate({x} {y}) scale({e})">'
            '<path d="M-95 265 L-70 75 Q0 35 70 75 L95 265 Z" fill="#9B6BC9"/>'
            f'<path d="M-40 60 Q0 105 40 60" fill="none" stroke="{SOL}" stroke-width="10" stroke-linecap="round"/>'
            '<circle cx="0" cy="-10" r="66" fill="#4A2E2A"/>'
            f'<circle cx="0" cy="0" r="58" fill="{PIEL}"/>'
            '<path d="M-60 -15 Q-58 -75 0 -77 Q58 -75 60 -15 Q40 -50 0 -50 Q-40 -50 -60 -15 Z" fill="#4A2E2A"/>'
            '<circle cx="0" cy="-90" r="24" fill="#4A2E2A"/>'
            f'<path d="M-46 -63 L-38 -105 L-20 -77 L0 -115 L20 -77 L38 -105 L46 -63 Z" fill="{SOL}" stroke="{ORO}" stroke-width="4" stroke-linejoin="round"/>'
            f'<circle cx="0" cy="-95" r="7" fill="{CORAL}"/>'
            f'<path d="M-28 0 q8 -9 16 0" fill="none" stroke="{TINTA}" stroke-width="5" stroke-linecap="round"/>'
            f'<path d="M12 0 q8 -9 16 0" fill="none" stroke="{TINTA}" stroke-width="5" stroke-linecap="round"/>'
            f'<circle cx="-34" cy="20" r="10" fill="{MEJILLA}" opacity="0.7"/><circle cx="34" cy="20" r="10" fill="{MEJILLA}" opacity="0.7"/>'
            f'<path d="M-16 30 Q0 45 16 30" fill="none" stroke="{TINTA}" stroke-width="5" stroke-linecap="round"/></g>')


def oso(x, y, e=1.0):
    return (f'<g transform="translate({x} {y}) scale({e})">'
            '<ellipse cx="0" cy="120" rx="130" ry="150" fill="#9A6A44"/>'
            '<ellipse cx="0" cy="150" rx="80" ry="100" fill="#C8996B"/>'
            '<circle cx="-80" cy="-90" r="34" fill="#9A6A44"/><circle cx="80" cy="-90" r="34" fill="#9A6A44"/>'
            '<circle cx="-80" cy="-90" r="16" fill="#C8996B"/><circle cx="80" cy="-90" r="16" fill="#C8996B"/>'
            '<circle cx="0" cy="-20" r="105" fill="#9A6A44"/>'
            '<ellipse cx="0" cy="20" rx="52" ry="40" fill="#C8996B"/>'
            f'<ellipse cx="0" cy="2" rx="18" ry="13" fill="{TINTA}"/>'
            f'<path d="M-50 -40 q12 -14 24 0" fill="none" stroke="{TINTA}" stroke-width="7" stroke-linecap="round"/>'
            f'<path d="M26 -40 q12 -14 24 0" fill="none" stroke="{TINTA}" stroke-width="7" stroke-linecap="round"/>'
            '<path d="M-14 34 Q0 48 14 34" fill="#E07B7B"/>'
            '<ellipse cx="22" cy="44" rx="10" ry="16" fill="#E07B7B"/>'
            '</g>')


def olimpo(x, y, e=1.0):
    return (f'<g transform="translate({x} {y}) scale({e})">'
            '<path d="M-330 300 L-60 -150 L0 -110 L60 -170 L330 300 Z" fill="#B7A6CF"/>'
            '<path d="M-60 -150 L-20 -84 L0 -110 L30 -70 L60 -170 L110 -86 L60 -100 L30 -50 L0 -80 L-30 -40 Z" fill="#FFFFFF"/>'
            '<g transform="translate(0 -205)">'
            '<path d="M-110 0 L0 -62 L110 0 Z" fill="#FBF7EF"/>'
            '<rect x="-112" y="0" width="224" height="14" fill="#F7F1E6"/>'
            '<g fill="#FBF7EF"><rect x="-98" y="14" width="18" height="80"/><rect x="-54" y="14" width="18" height="80"/>'
            '<rect x="-9" y="14" width="18" height="80"/><rect x="36" y="14" width="18" height="80"/><rect x="80" y="14" width="18" height="80"/></g>'
            '<rect x="-118" y="94" width="236" height="16" fill="#F7F1E6"/></g>'
            '<g fill="#FFFFFF"><ellipse cx="-90" cy="-90" rx="120" ry="34"/><ellipse cx="80" cy="-84" rx="130" ry="36"/><ellipse cx="0" cy="-70" rx="90" ry="30"/></g>'
            '</g>')


def destellos(puntos, color="#FFFFFF"):
    return "".join(
        f'<path transform="translate({x} {y}) scale({s})" d="M0 -22 l6 16 16 6 -16 6 -6 16 -6 -16 -16 -6 16 -6 Z" fill="{color}"/>'
        for x, y, s in puntos)


def rastro(d):
    return (f'<path d="{d}" fill="none" stroke="{TINTA}" stroke-width="4" stroke-dasharray="4 14" '
            'stroke-linecap="round" opacity="0.45"/>')


W, H = 1600, 1000

escenas = {}

# 1. La colmena en el monte Himeto
escenas["img_1"] = lienzo(W, H, "".join([
    f'<rect width="{W}" height="{H}" fill="url(#cielo)"/>',
    f'<circle cx="1300" cy="200" r="80" fill="{SOL}"/>',
    nube(330, 190), nube(900, 140, 0.8),
    colinas(W, H),
    colmena(560, 760, 1.1),
    tomillo(180, 900), tomillo(260, 930, 0.8), tomillo(1500, 900), tomillo(1420, 940, 0.9),
    flor(880, 840, "#FFFFFF"), flor(980, 870, CORAL, 0.9), flor(1180, 830, "#FFFFFF", 1.1), flor(1300, 880, "#B48AD8", 0.9),
    obrera(820, 520, mira="derecha", giro=-10), obrera(330, 470, giro=10), obrera(1080, 640, mira="derecha"),
    rastro("M760 560 Q800 470 860 500"),
    abeja(1000, 470, 1.25, "izquierda", "alegre"),
]))

# 2. Los que se llevan la miel
escenas["img_2"] = lienzo(W, H, "".join([
    f'<rect width="{W}" height="{H}" fill="url(#cielo)"/>',
    f'<circle cx="220" cy="180" r="70" fill="{SOL}"/>',
    nube(1250, 180), colinas(W, H),
    colmena(1150, 780, 1.0),
    oso(700, 560, 1.15),
    # pata del oso con miel
    '<g transform="translate(860 600)"><ellipse cx="0" cy="0" rx="48" ry="40" fill="#9A6A44"/>'
    '<path d="M-30 -30 Q0 -50 30 -30 Q40 10 20 30 Q10 60 0 30 Q-20 40 -30 -30 Z" fill="#FFC94D"/></g>',
    tarro(560, 780, 0.9),
    '<path d="M560 780 m-30 8 q0 30 8 40" fill="none" stroke="#FFC94D" stroke-width="10" stroke-linecap="round"/>',
    tomillo(150, 920), flor(1450, 880, "#FFFFFF"), flor(300, 900, CORAL, 0.9),
    abeja(1230, 380, 1.2, "izquierda", "triste"),
    obrera(1380, 480, giro=8), obrera(1090, 300, giro=-6),
]))

# 3. El vuelo hasta el Olimpo
escenas["img_3"] = lienzo(W, H, "".join([
    f'<rect width="{W}" height="{H}" fill="url(#cielo)"/>',
    f'<circle cx="1180" cy="200" r="260" fill="url(#luz)"/>',
    olimpo(1180, 560, 1.15),
    nube(260, 260, 1.1), nube(620, 520, 1.3), nube(200, 760, 1.4), nube(900, 860, 1.6), nube(1450, 880, 1.5),
    rastro("M180 900 Q300 700 420 680 Q560 660 620 560"),
    abeja(700, 470, 1.25, "derecha", "alegre", giro=-14),
    tarro(620, 560, 0.55),
    destellos([(1180, 230, 1.2), (1320, 300, 0.9), (1050, 300, 0.8)]),
]))

# 4. El banquete de bodas (la muestra ya aprobada)
escenas["img_4"] = Path(__file__).with_name("la_abeja_y_zeus_banquete.svg").read_text(encoding="utf-8")

# 5. El deseo: la abeja habla muy seria con Zeus
escenas["img_5"] = lienzo(W, H, "".join([
    f'<rect width="{W}" height="{H}" fill="url(#cielo)"/>',
    f'<circle cx="1000" cy="420" r="560" fill="url(#luz)"/>',
    nube(200, 200), nube(1450, 160, 0.9),
    zeus(1040, 420, 1.5, "alegre"),
    abeja(520, 430, 1.7, "derecha", "seria"),
    # bocadillo con un aguijón dibujado: lo que pide
    '<g transform="translate(360 190)"><ellipse cx="0" cy="0" rx="120" ry="80" fill="#FFFFFF"/>'
    '<path d="M60 60 L110 120 L20 72 Z" fill="#FFFFFF"/>'
    f'<path d="M-50 0 L50 -14 L50 14 Z" fill="{TINTA}"/></g>',
    '<g fill="#FFFFFF"><ellipse cx="160" cy="930" rx="300" ry="120"/><ellipse cx="560" cy="950" rx="320" ry="120"/>'
    '<ellipse cx="1000" cy="945" rx="340" ry="125"/><ellipse cx="1450" cy="930" rx="320" ry="125"/><rect x="0" y="940" width="1600" height="60"/></g>',
]))

# 6. Zeus frunce el ceño, pero cumple su promesa
escenas["img_6"] = lienzo(W, H, "".join([
    f'<rect width="{W}" height="{H}" fill="url(#tormenta)"/>',
    nube(250, 180, 1.1, 0.7), nube(1350, 150, 1.0, 0.7),
    zeus(980, 420, 1.5, "ceño", rayo=True),
    abeja(470, 520, 1.6, "derecha", "pensativa", aguijon_brillo=True),
    destellos([(380, 380, 0.9), (600, 650, 0.7)], "#FFF3B0"),
    '<g fill="#FFFFFF"><ellipse cx="160" cy="930" rx="300" ry="120"/><ellipse cx="560" cy="950" rx="320" ry="120"/>'
    '<ellipse cx="1000" cy="945" rx="340" ry="125"/><ellipse cx="1450" cy="930" rx="320" ry="125"/><rect x="0" y="940" width="1600" height="60"/></g>',
]))

# 7. De vuelta a casa, al atardecer
escenas["img_7"] = lienzo(W, H, "".join([
    f'<rect width="{W}" height="{H}" fill="url(#atardecer)"/>',
    f'<circle cx="1200" cy="560" r="120" fill="#FFB15E"/>',
    nube(350, 200, 1.0, 0.8), nube(900, 150, 0.8, 0.8),
    colinas(W, H, "#B8D99A", "#7FBF7A"),
    colmena(420, 790, 1.0),
    flor(760, 860, "#FFFFFF"), flor(860, 900, CORAL, 0.9), flor(1000, 860, "#FFFFFF", 1.1), flor(1130, 900, "#B48AD8"),
    flor(1300, 870, "#FFFFFF"), flor(1450, 900, CORAL, 0.9), tomillo(150, 930),
    obrera(800, 640, mira="derecha", giro=-8), obrera(1020, 700), obrera(1350, 650, mira="derecha"),
    obrera(620, 520),
    abeja(1000, 460, 1.2, "izquierda", "alegre"),
    rastro("M1460 360 Q1260 330 1100 430"),
]))

# Portada (cuadrada): la abeja reina vuela al Olimpo con su miel
escenas["portada"] = lienzo(1000, 1000, "".join([
    '<rect width="1000" height="1000" fill="url(#cielo)"/>',
    '<circle cx="680" cy="260" r="320" fill="url(#luz)"/>',
    olimpo(700, 520, 0.9),
    nube(150, 250, 1.0), nube(250, 800, 1.5), nube(800, 880, 1.6),
    rastro("M80 700 Q200 560 330 560"),
    tarro(340, 600, 0.75),
    abeja(430, 500, 1.9, "derecha", "alegre", giro=-10),
    destellos([(700, 200, 1.1), (840, 280, 0.8)]),
]))

for nombre, svg in escenas.items():
    (SALIDA / f"{nombre}.svg").write_text(svg, encoding="utf-8")
    (SALIDA / f"{nombre}.png").write_bytes(bytes(resvg_py.svg_to_bytes(svg_string=svg)))
    print(nombre, "ok")
