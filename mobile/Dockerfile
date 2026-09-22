# syntax=docker/dockerfile:1
#
# Compila o app Android, roda os testes unitarios e exporta o APK, sem precisar do Android Studio:
#
#   docker build --build-arg ACCEPT_ANDROID_SDK_LICENSES=yes \
#       --build-arg API_BASE_URL=http://192.168.0.10:8080/ \
#       --target artefatos --output type=local,dest=./saida .
#
# Resultado: ./saida/aguia-inova-debug.apk e ./saida/relatorio-testes
#
# Ao passar ACCEPT_ANDROID_SDK_LICENSES=yes voce aceita os termos do Android SDK:
# https://developer.android.com/studio/terms

# ---- base: JDK 17 + Android SDK (plataforma 36 e build-tools 36.0.0) ----
FROM eclipse-temurin:17-jdk AS base
ARG ACCEPT_ANDROID_SDK_LICENSES=no
ARG CMDLINE_TOOLS_VERSION=16111833
ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH="${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin"

RUN test "${ACCEPT_ANDROID_SDK_LICENSES}" = "yes" \
    || (echo "Defina --build-arg ACCEPT_ANDROID_SDK_LICENSES=yes para aceitar as licencas do Android SDK." >&2; exit 1)
RUN apt-get update && apt-get install -y --no-install-recommends unzip curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*
RUN mkdir -p "${ANDROID_HOME}/cmdline-tools" \
    && curl -fsSL "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip" -o /tmp/tools.zip \
    && unzip -q /tmp/tools.zip -d "${ANDROID_HOME}/cmdline-tools" \
    && mv "${ANDROID_HOME}/cmdline-tools/cmdline-tools" "${ANDROID_HOME}/cmdline-tools/latest" \
    && rm /tmp/tools.zip
RUN yes | sdkmanager --licenses > /dev/null \
    && sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"

# ---- build: compila, testa e gera o APK ----
FROM base AS build
WORKDIR /project
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
COPY app app
ARG API_BASE_URL=http://10.0.2.2:8080/
RUN ./gradlew --no-daemon --console=plain -PapiBaseUrl=${API_BASE_URL} testDebugUnitTest assembleDebug

# ---- artefatos: so o que interessa exportar ----
FROM scratch AS artefatos
COPY --from=build /project/app/build/outputs/apk/debug/app-debug.apk /aguia-inova-debug.apk
COPY --from=build /project/app/build/reports/tests/testDebugUnitTest /relatorio-testes
