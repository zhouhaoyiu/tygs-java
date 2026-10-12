import { execFileSync } from "node:child_process";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

const file = resolve("target/dependency-audit.json");
execFileSync("mvn", ["-B", "-q", "dependency:tree", "-DoutputType=json", `-DoutputFile=${file}`], { stdio: "inherit" });
const root = JSON.parse(readFileSync(file, "utf8"));
const packages = new Map();
function visit(node) {
  if (node !== root) {
    const name = `${node.groupId}:${node.artifactId}`;
    packages.set(`${name}@${node.version}`, { package: { ecosystem: "Maven", name }, version: node.version });
  }
  for (const child of node.children || []) visit(child);
}
visit(root);
const queries = [...packages.values()];
if (!queries.length) throw new Error("No resolved dependencies were found");
const response = await fetch("https://api.osv.dev/v1/querybatch", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ queries }),
  signal: AbortSignal.timeout(60000),
});
if (!response.ok) throw new Error(`Dependency audit failed: HTTP ${response.status}`);
const { results } = await response.json();
if (!Array.isArray(results) || results.length !== queries.length) throw new Error("Incomplete dependency audit response");
let count = 0;
for (let i = 0; i < results.length; i++) {
  for (const vulnerability of results[i].vulns || []) {
    count++;
    console.error(`${queries[i].package.name}@${queries[i].version}: ${vulnerability.id}`);
  }
  if (results[i].next_page_token) throw new Error("Additional vulnerabilities require review");
}
console.log(`Checked ${queries.length} resolved dependencies: ${count} known vulnerabilities`);
if (count) process.exitCode = 1;
