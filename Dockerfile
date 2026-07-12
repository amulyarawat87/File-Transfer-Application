# =============================================================================
# BUILD STAGE — MISSING
# =============================================================================
# CODE REVIEW [DevOps]: No multi-stage build — Maven compile/package is done on EC2 HOST before `docker build`
# (see deploy-backend.yml: `mvn clean package -DskipTests`). `docker build` alone fails with no target/*.jar.
# ADD: Stage 1 → FROM eclipse-temurin:21-jdk AS builder → COPY pom.xml + src → RUN ./mvnw package -DskipTests
# ADD: Stage 2 → FROM eclipse-temurin:21-jre → COPY --from=builder target/*.jar app.jar

# CODE REVIEW [DevOps]: No Maven Wrapper (mvnw) copied into build stage — build depends on Maven installed on host EC2.
# CODE REVIEW [DevOps]: No .mvn/ directory in Docker build context — reproducible builds need mvnw inside the image build.
# CODE REVIEW [Reliability]: No build-arg for version/profile — image tag is not tied to pom.xml version (0.0.1-SNAPSHOT).

# =============================================================================
# RUNTIME BASE IMAGE
# =============================================================================
# CODE REVIEW [DevOps]: Uses JRE only (correct for runtime) but JDK is required separately on host for mvn package.
FROM eclipse-temurin:21-jre

# CODE REVIEW [DevOps]: No LABEL metadata — add maintainer, version, git-sha for traceability in registries.
# CODE REVIEW [DevOps]: No ARG/ENV for SPRING_PROFILES_ACTIVE — container defaults to profile "dev" unless --env-file sets it.

WORKDIR /app

# =============================================================================
# APPLICATION ARTIFACT — PARTIALLY PRESENT
# =============================================================================
# CODE REVIEW [Reliability]: COPY target/*.jar fails if jar does not exist (no prior mvn package) or if multiple jars match.
# FIX: COPY target/FileTransferApplication-0.0.1-SNAPSHOT.jar app.jar
# CODE REVIEW [DevOps]: Only the Spring Boot fat JAR is copied — this IS correct for app Maven deps (they are inside the JAR):
#   ✓ spring-boot-starter-webmvc, spring-boot-starter-data-jpa (inside JAR)
#   ✓ postgresql JDBC driver (inside JAR — driver yes, server no)
#   ✓ software.amazon.awssdk:s3 (inside JAR — SDK yes, S3 service no)
#   ✓ dotenv-java, bcprov-jdk15on (inside JAR)
#   ✗ lombok, devtools — compile-time only, correctly excluded from runtime
COPY target/*.jar app.jar

# =============================================================================
# MISSING: POSTGRESQL DATABASE SERVER
# =============================================================================
# CODE REVIEW [Architecture]: PostgreSQL is NOT in this image — required external service.
#   App needs: DB_URL, DB_USERNAME, DB_PASSWORD (from application.properties).
#   deploy-backend.yml starts Postgres on EC2 host via systemctl — NOT inside this container.
# CODE REVIEW [Reliability]: DB_URL=jdbc:postgresql://localhost:5432/... WILL FAIL inside container —
#   localhost inside Docker = the container itself, not the EC2 host running Postgres.
#   FIX options: jdbc:postgresql://172.17.0.1:5432/db  OR  docker run --network host  OR  docker-compose with postgres service.
# CODE REVIEW [DevOps]: No wait-for-it / wait-for-db script — app may crash on startup if Postgres is not ready yet.
# CODE REVIEW [DevOps]: No docker-compose.yml linking app + postgres on a shared network — manual wiring required today.

# =============================================================================
# MISSING: AWS S3 (EXTERNAL CLOUD SERVICE)
# =============================================================================
# CODE REVIEW [Architecture]: AWS S3 is NOT in this image — required external service.
#   App needs: AWS_REGION, AWS_S3_BUCKET, AWS_ACCESS_KEY, AWS_SECRET_KEY.
# CODE REVIEW [Reliability]: Container must have outbound HTTPS (port 443) to S3 endpoint — no VPC/firewall rules defined here.
# CODE REVIEW [Security]: Long-lived AWS_ACCESS_KEY/AWS_SECRET_KEY passed via --env-file — prefer IAM role on EC2 instance instead.

# =============================================================================
# MISSING: ENVIRONMENT / SECRETS CONFIGURATION
# =============================================================================
# CODE REVIEW [DevOps]: No ENV or COPY for config — all secrets injected at runtime via:
#   docker run --env-file /home/ec2-user/.../.env  (see deploy-backend.yml)
# CODE REVIEW [Reliability]: Required env vars NOT declared in Dockerfile (no ENV placeholders, no validation at build time):
#   DB_URL, DB_USERNAME, DB_PASSWORD
#   AWS_REGION, AWS_S3_BUCKET, AWS_ACCESS_KEY, AWS_SECRET_KEY
#   SPRING_PROFILES_ACTIVE (optional, default: dev)
#   SPRING_JPA_SHOW_SQL, SPRING_JPA_DDL_AUTO (optional)
# CODE REVIEW [Reliability]: DotenvEnvironmentPostProcessor loads .env.dev / .env.prod from filesystem —
#   those files are gitignored and NOT COPY'd into this image, so dotenv silently no-ops (ignoreIfMissing).
#   Runtime config depends entirely on docker --env-file / -e injection.
# CODE REVIEW [Security]: Do NOT COPY .env / .env.prod into the image — secrets would be baked into image layers.

# =============================================================================
# MISSING: NETWORK / PORT CONFIG
# =============================================================================
# CODE REVIEW [DevOps]: EXPOSE 8080 is documentation only — does not publish the port.
#   Still required: docker run -p 8080:8080 (present in deploy-backend.yml).
# CODE REVIEW [DevOps]: No --add-host=host.docker.internal in deploy script — needed on Linux to reach host Postgres from container.
# CODE REVIEW [DevOps]: No docker network defined — app container uses default bridge; host Postgres reachability is fragile.

# =============================================================================
# MISSING: JVM / PROCESS CONFIG
# =============================================================================
# CODE REVIEW [Reliability]: No JVM memory flags — large file downloads load entire file into heap; container may OOMKill.
#   ADD: ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-XX:+UseContainerSupport","-jar","app.jar"]
# CODE REVIEW [Reliability]: No graceful shutdown config — Spring may not drain in-flight requests on docker stop.
#   ADD: -Dspring.lifecycle.timeout-per-shutdown-phase=30s  and  STOPSIGNAL SIGTERM
# CODE REVIEW [DevOps]: No JAVA_TOOL_OPTIONS env — common hook for runtime JVM tuning without rebuilding image.

# CODE REVIEW [DevOps]: No HEALTHCHECK — orchestrators (ECS/K8s/Docker) cannot detect hung or dead app.
#   Requires spring-boot-starter-actuator dependency (currently MISSING from pom.xml) + endpoint:
#   HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD curl -f http://localhost:8080/actuator/health || exit 1
# CODE REVIEW [DevOps]: No curl/wget installed in JRE image for healthcheck — would need RUN apt-get install curl or use JVM-based check.

EXPOSE 8080

# =============================================================================
# MISSING: SECURITY HARDENING
# =============================================================================
# CODE REVIEW [Security]: Container runs as root — add non-root user:
#   RUN groupadd -r app && useradd -r -g app app && chown -R app:app /app && USER app
# CODE REVIEW [Security]: No read-only root filesystem — writable /app allows runtime tampering.
#   ADD: docker run --read-only --tmpfs /tmp
# CODE REVIEW [Security]: No image vulnerability scanning step in CI — add trivy/snyk in deploy pipeline.

# =============================================================================
# MISSING: OBSERVABILITY / OPS
# =============================================================================
# CODE REVIEW [Observability]: No log driver or log format configured — stdout only; no structured JSON logging for containers.
# CODE REVIEW [Observability]: spring-boot-starter-actuator NOT in pom.xml — no /actuator/health, /actuator/metrics endpoints.
# CODE REVIEW [DevOps]: No container resource limits documented — add docker run --memory=512m --cpus=1 to prevent host exhaustion.
# CODE REVIEW [DevOps]: No timezone set — JVM uses UTC by default; set ENV TZ=Asia/Kolkata if logs/timestamps must match locale.

# =============================================================================
# ENTRYPOINT — MINIMAL (PRESENT BUT INCOMPLETE)
# =============================================================================
ENTRYPOINT ["java","-jar","app.jar"]

# =============================================================================
# QUICK REFERENCE: WHAT YOU MUST PROVIDE OUTSIDE THIS DOCKERFILE
# =============================================================================
# CODE REVIEW [Architecture]: Complete runtime checklist for docker run:
#   1. Build JAR first:     mvn clean package -DskipTests
#   2. Build image:         docker build -t file-transfer-application .
#   3. Start PostgreSQL:    systemctl start postgresql  (on host, NOT in container)
#   4. Fix DB_URL:          use host IP / host.docker.internal, NOT localhost
#   5. Provide env file:    --env-file .env  with all required vars listed above
#   6. Publish port:        -p 8080:8080
#   7. (Recommended)        --restart=always  --memory=512m  --network host (if Postgres on same host)
