// @ts-check

/** @type {import("lint-staged").Configuration} */
export default {
	"./**/*.{ts,cts,mts,js,cjs,mjs,json,sh}": "pnpm exec biome lint",
	"./**/*.{yaml,yml,md}": "pnpm exec prettier --cache --write",
	"./**/!(*.md|*.yaml|*.yml|*.kts)": "pnpm exec biome format --write",
};
