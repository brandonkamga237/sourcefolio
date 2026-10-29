# syntax=docker/dockerfile:1
FROM node:24.21-alpine AS deps
WORKDIR /app
# Corepack installs the pnpm version pinned by "packageManager" in package.json, so the
# image builds with the same pnpm as the developers and CI, never whatever is newest.
ENV COREPACK_ENABLE_DOWNLOAD_PROMPT=0
RUN corepack enable
# pnpm-workspace.yaml lists the packages allowed to run build scripts (sharp, esbuild),
# otherwise pnpm refuses to run them in CI until they are approved interactively.
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN --mount=type=cache,target=/root/.local/share/pnpm/store pnpm install --frozen-lockfile

FROM deps AS build
COPY . .
ENV NEXT_TELEMETRY_DISABLED=1
# next.config sets output: 'standalone', which emits a minimal server.js with only the
# dependencies the app imports.
RUN pnpm build

FROM node:24.21-alpine AS runtime
WORKDIR /app
ENV NODE_ENV=production NEXT_TELEMETRY_DISABLED=1 PORT=3000 HOSTNAME=0.0.0.0
COPY --from=build --chown=node:node /app/.next/standalone ./
COPY --from=build --chown=node:node /app/.next/static ./.next/static
COPY --from=build --chown=node:node /app/public ./public
USER node
EXPOSE 3000
CMD ["node", "server.js"]
