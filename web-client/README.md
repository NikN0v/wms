# WMS Web Client

Vue 3 + TypeScript single-page application built with Vite.

## Development

```sh
npm install
npm run dev
```

Vite proxies `/api` to `http://localhost:8080` by default. Override the target with `VITE_API_PROXY_TARGET` or use `VITE_API_BASE_URL` for a separately hosted API. Start from `.env.example` when creating a local `.env` file.

## Structure

- `src/app` — router and Pinia application stores.
- `src/layouts` — reusable application layouts and navigation.
- `src/views` — route-level pages.
- `src/shared/api` — configured Axios client and API error handling.
- `src/shared/components` — reusable, styled interface components.
- `src/shared/feedback` — common notification API.
- `src/styles/tokens.css` — global palette, radii, shadows, and Tailwind theme tokens.

Element Plus is the base component system. Tailwind CSS is available for composable utility styling; shared application components and theme tokens remain the source of visual consistency.

## Build

```sh
npm run build
```
