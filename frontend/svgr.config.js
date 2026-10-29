// icons/*.svg → components/icons/*.tsx, plus an index exporting IconXxx. The SVG files are
// the source of truth: the generated components are never edited by hand.
module.exports = {
  typescript: true,
  jsxRuntime: "automatic",
  filenameCase: "pascal",
  svgo: false,
  prettier: false,
  svgProps: { "aria-hidden": "true", focusable: "false" },
  indexTemplate: (files) =>
    files
      .map((file) => file.path.split("/").pop().replace(/\.tsx?$/, ""))
      .sort()
      .map((name) => `export { default as Icon${name} } from "./${name}";`)
      .join("\n") + "\n",
};
