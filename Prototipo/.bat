@echo off
REM ============================================================
REM  Crea la estructura de carpetas y archivos vacios
REM  del Sprint 1 - Home page (Club Nautico Poseidon del Peru)
REM
REM  Como usarlo:
REM  1) Copia este archivo dentro de la carpeta del proyecto
REM     Angular (donde ya existe la carpeta "src").
REM  2) Doble clic, o desde consola: crear_estructura.bat
REM ============================================================

setlocal

set BASE=src\app\components
set STYLES=src\styles\abstracts
set ASSETS=public\assets\imagenes

if not exist src (
    echo No se encontro la carpeta "src". Ejecuta este .bat
    echo dentro de la raiz de tu proyecto Angular ^(ng new^).
    pause
    exit /b 1
)

echo Creando carpetas...

REM ---------- Header ----------
mkdir "%BASE%\header" 2>nul
type nul > "%BASE%\header\header.html"
type nul > "%BASE%\header\header.scss"
type nul > "%BASE%\header\header.spec.ts"
type nul > "%BASE%\header\header.ts"

REM ---------- Footer ----------
mkdir "%BASE%\footer" 2>nul
type nul > "%BASE%\footer\footer.html"
type nul > "%BASE%\footer\footer.scss"
type nul > "%BASE%\footer\footer.spec.ts"
type nul > "%BASE%\footer\footer.ts"

REM ---------- Home (contenedor) ----------
mkdir "%BASE%\home" 2>nul
type nul > "%BASE%\home\home.html"
type nul > "%BASE%\home\home.scss"
type nul > "%BASE%\home\home.spec.ts"
type nul > "%BASE%\home\home.ts"

REM ---------- Home > Hero ----------
mkdir "%BASE%\home\hero" 2>nul
type nul > "%BASE%\home\hero\hero.html"
type nul > "%BASE%\home\hero\hero.scss"
type nul > "%BASE%\home\hero\hero.spec.ts"
type nul > "%BASE%\home\hero\hero.ts"

REM ---------- Home > Nosotros ----------
mkdir "%BASE%\home\nosotros" 2>nul
type nul > "%BASE%\home\nosotros\nosotros.html"
type nul > "%BASE%\home\nosotros\nosotros.scss"
type nul > "%BASE%\home\nosotros\nosotros.spec.ts"
type nul > "%BASE%\home\nosotros\nosotros.ts"

REM ---------- Home > Servicios ----------
mkdir "%BASE%\home\servicios" 2>nul
type nul > "%BASE%\home\servicios\servicios.html"
type nul > "%BASE%\home\servicios\servicios.scss"
type nul > "%BASE%\home\servicios\servicios.spec.ts"
type nul > "%BASE%\home\servicios\servicios.ts"

REM ---------- Home > Instalaciones ----------
mkdir "%BASE%\home\instalaciones" 2>nul
type nul > "%BASE%\home\instalaciones\instalaciones.html"
type nul > "%BASE%\home\instalaciones\instalaciones.scss"
type nul > "%BASE%\home\instalaciones\instalaciones.spec.ts"
type nul > "%BASE%\home\instalaciones\instalaciones.ts"

REM ---------- Home > Contacto ----------
mkdir "%BASE%\home\contacto" 2>nul
type nul > "%BASE%\home\contacto\contacto.html"
type nul > "%BASE%\home\contacto\contacto.scss"
type nul > "%BASE%\home\contacto\contacto.spec.ts"
type nul > "%BASE%\home\contacto\contacto.ts"

REM ---------- Estilos globales (abstracts) ----------
mkdir "%STYLES%" 2>nul
type nul > "%STYLES%\_shared.scss"

REM ---------- Carpetas de imagenes (placeholders) ----------
mkdir "%ASSETS%\hero" 2>nul
mkdir "%ASSETS%\servicios" 2>nul
mkdir "%ASSETS%\instalaciones" 2>nul

echo. > "%ASSETS%\hero\.gitkeep"
echo. > "%ASSETS%\servicios\.gitkeep"
echo. > "%ASSETS%\instalaciones\.gitkeep"

echo.
echo ============================================================
echo  Listo. Estructura creada correctamente:
echo    %BASE%\header
echo    %BASE%\footer
echo    %BASE%\home  (+ hero, nosotros, servicios, instalaciones, contacto)
echo    %STYLES%\_shared.scss
echo    %ASSETS%\hero  %ASSETS%\servicios  %ASSETS%\instalaciones
echo ============================================================
echo.
pause