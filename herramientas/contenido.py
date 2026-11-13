"""
Genera el contenido de la app a partir de contenido/cuentos/<id>/:

    cuento.json      título, descripción y líneas en los tres idiomas
    portada.(jpg|png|webp)
    img_<N>.(png|jpg|webp)   ilustraciones que citan las líneas

Valida todo, convierte las imágenes a WebP (la portada en miniatura) y escribe
catalogo.json. Los nombres de las imágenes llevan un resumen de su contenido, así
que una imagen cambiada nunca se confunde con la que ya tenga el móvil en caché.

Uso (desde la raíz del proyecto, con Pillow instalado: pip install -r herramientas/requirements.txt):

    python herramientas/contenido.py            valida y genera
    python herramientas/contenido.py --validar  solo valida

Salidas:
    app/src/main/assets/contenido/   lo que va dentro de la app (funciona sin conexión)
    build/contenido-web/contenido/   lo que se publica en GitHub Pages para actualizar la app
"""
import argparse
import hashlib
import io
import json
import shutil
import sys
import time
from pathlib import Path

from PIL import Image

RAIZ = Path(__file__).resolve().parent.parent
FUENTES = RAIZ / 'contenido' / 'cuentos'
SALIDA_APP = RAIZ / 'app' / 'src' / 'main' / 'assets' / 'contenido'
SALIDA_WEB = RAIZ / 'build' / 'contenido-web' / 'contenido'

IDIOMAS = ('esp', 'cat', 'eng')
FINALES = {'esp': 'FIN', 'cat': 'FI', 'eng': 'THE END'}
EXTENSIONES = ('.webp', '.png', '.jpg', '.jpeg')

# Las ilustraciones se ven a pantalla completa; las portadas, en la lista (~110-160 dp)
ANCHO_MAX_ILUSTRACION = 1600
LADO_MAX_PORTADA = 480
CALIDAD_WEBP = 82

# Versión del formato de catalogo.json; la app ignora catálogos de versiones que no conoce
FORMATO = 1


class ErrorContenido(Exception):
    pass


def buscar_imagen(carpeta: Path, nombre: str) -> Path:
    for ext in EXTENSIONES:
        ruta = carpeta / f'{nombre}{ext}'
        if ruta.exists():
            return ruta
    raise ErrorContenido(f'{carpeta.name}: falta la imagen {nombre} ({"/".join(EXTENSIONES)})')


def validar(carpeta: Path) -> dict:
    ruta = carpeta / 'cuento.json'
    if not ruta.exists():
        raise ErrorContenido(f'{carpeta.name}: falta cuento.json')
    cuento = json.loads(ruta.read_text(encoding='utf-8'))
    cid = cuento.get('id')
    if cid != carpeta.name:
        raise ErrorContenido(f'{carpeta.name}: el id ({cid}) debe llamarse igual que la carpeta')

    for campo in ('titulo', 'descripcion'):
        for idioma in IDIOMAS:
            if not str(cuento.get(campo, {}).get(idioma, '')).strip():
                raise ErrorContenido(f'{cid}: falta {campo} en {idioma}')

    lineas = cuento.get('lineas') or []
    if not lineas:
        raise ErrorContenido(f'{cid}: no tiene líneas')
    for n, linea in enumerate(lineas, start=1):
        for idioma in IDIOMAS:
            if not str(linea.get(idioma, '')).strip():
                raise ErrorContenido(f'{cid}: la línea {n} no tiene texto en {idioma}')
    if lineas[0].get('imagen') is None:
        raise ErrorContenido(f'{cid}: la primera línea tiene que indicar una ilustración')
    for idioma, final in FINALES.items():
        if lineas[-1][idioma].strip().upper() != final:
            raise ErrorContenido(f'{cid}: la última línea en {idioma} debe ser "{final}"')

    buscar_imagen(carpeta, 'portada')
    for numero in sorted({l['imagen'] for l in lineas if l.get('imagen')}):
        buscar_imagen(carpeta, f'img_{numero}')
    return cuento


def a_webp(origen: Path, lado_max: int, solo_ancho: bool) -> bytes:
    with Image.open(origen) as imagen:
        imagen = imagen.convert('RGBA' if imagen.mode in ('RGBA', 'LA', 'P') else 'RGB')
        ancho, alto = imagen.size
        escala = lado_max / (ancho if solo_ancho else max(ancho, alto))
        if escala < 1:
            imagen = imagen.resize((round(ancho * escala), round(alto * escala)), Image.LANCZOS)
        salida = io.BytesIO()
        imagen.save(salida, 'WEBP', quality=CALIDAD_WEBP, method=6)
        return salida.getvalue()


def guardar_imagen(datos: bytes, cid: str, nombre: str, destino: Path) -> str:
    resumen = hashlib.sha256(datos).hexdigest()[:10]
    relativa = f'cuentos/{cid}/{nombre}-{resumen}.webp'
    (destino / relativa).parent.mkdir(parents=True, exist_ok=True)
    (destino / relativa).write_bytes(datos)
    return relativa


def generar(cuentos: list, temporal: Path) -> dict:
    catalogo = []
    for cuento in cuentos:
        cid = cuento['id']
        carpeta = FUENTES / cid
        portada = guardar_imagen(
            a_webp(buscar_imagen(carpeta, 'portada'), LADO_MAX_PORTADA, solo_ancho=False),
            cid, 'portada', temporal
        )
        ilustraciones = {}
        for numero in sorted({l['imagen'] for l in cuento['lineas'] if l.get('imagen')}, key=int):
            datos = a_webp(buscar_imagen(carpeta, f'img_{numero}'), ANCHO_MAX_ILUSTRACION, solo_ancho=True)
            ilustraciones[numero] = guardar_imagen(datos, cid, f'img_{numero}', temporal)
        catalogo.append({
            'id': cid,
            'edad': cuento.get('edad'),
            'titulo': {i: cuento['titulo'][i].strip() for i in IDIOMAS},
            'descripcion': {i: cuento['descripcion'][i].strip() for i in IDIOMAS},
            'portada': portada,
            'ilustraciones': ilustraciones,
            'lineas': [
                {'imagen': l.get('imagen'), 'texto': {i: l[i].strip() for i in IDIOMAS}}
                for l in cuento['lineas']
            ],
        })
    contenido = json.dumps(catalogo, ensure_ascii=False, sort_keys=True)
    return {
        'formato': FORMATO,
        # La app se queda con el catálogo más reciente entre el incluido y el descargado
        'generado': int(time.time()),
        'revision': hashlib.sha256(contenido.encode('utf-8')).hexdigest()[:12],
        'cuentos': catalogo,
    }


def escribir(catalogo: dict, temporal: Path, destino: Path):
    if destino.exists():
        shutil.rmtree(destino)
    shutil.copytree(temporal, destino)
    (destino / 'catalogo.json').write_text(
        json.dumps(catalogo, ensure_ascii=False, separators=(',', ':')), encoding='utf-8'
    )


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('--validar', action='store_true', help='solo valida, sin generar nada')
    args = parser.parse_args()

    try:
        cuentos = [validar(c) for c in sorted(FUENTES.iterdir()) if c.is_dir()]
    except ErrorContenido as e:
        sys.exit(f'ERROR: {e}')
    print(f'{len(cuentos)} cuentos correctos')
    if args.validar:
        return

    temporal = RAIZ / 'build' / 'contenido-tmp'
    if temporal.exists():
        shutil.rmtree(temporal)
    temporal.mkdir(parents=True)
    catalogo = generar(cuentos, temporal)

    # Si el contenido no ha cambiado se conserva la fecha, para no forzar descargas en los móviles
    anterior = SALIDA_APP / 'catalogo.json'
    if anterior.exists():
        previo = json.loads(anterior.read_text(encoding='utf-8'))
        if previo.get('revision') == catalogo['revision']:
            catalogo['generado'] = previo['generado']

    escribir(catalogo, temporal, SALIDA_APP)
    escribir(catalogo, temporal, SALIDA_WEB)
    shutil.rmtree(temporal)

    total = sum(f.stat().st_size for f in SALIDA_APP.rglob('*') if f.is_file())
    print(f'Revisión {catalogo["revision"]}: {total / 1024:.0f} KB en {SALIDA_APP.relative_to(RAIZ)}')
    print(f'Para publicar: copia {SALIDA_WEB.relative_to(RAIZ)} a la rama gh-pages')


if __name__ == '__main__':
    main()
