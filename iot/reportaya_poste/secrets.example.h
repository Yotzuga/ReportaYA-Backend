// Copia este archivo como secrets.h (esta en .gitignore) y completa tus datos.
#pragma once

#define RY_WIFI_SSID "TU_RED_WIFI"
#define RY_WIFI_PASS "TU_PASSWORD_WIFI"

// Backend:
//   Nube:     "https://reportaya-backend.onrender.com"
//   PC local: "http://192.168.18.8:8081"   (misma red y puerto 8081 abierto)
#define RY_BASE_URL  "https://reportaya-backend.onrender.com"

// Cuenta de servicio creada por db/seed-cuenta-iot.sql
#define RY_SVC_USER  "poste_iot"
#define RY_SVC_PASS  "poste123"

// Descomenta solo para pruebas si cambias a un servidor https con otra CA:
// #define RY_TLS_INSECURE
