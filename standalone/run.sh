#!/usr/bin/env bash
#
# Convenience wrapper for running a standalone program.
#
# Usage:
#   standalone/run.sh <fully.qualified.MainClass> [program args...]
#
# Examples:
#   standalone/run.sh coppercore.standalone.HelloCopperCore
#   standalone/run.sh coppercore.standalone.TransformJsonDump
#   standalone/run.sh my.pkg.MyProgram foo bar
#
# Runs Gradle quietly (-q) so only your program's output appears on stdout.
# Works from any directory; it locates the repo root relative to this script.

set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${script_dir}/.." && pwd)"

if [ "$#" -lt 1 ]; then
	echo "Usage: $0 <fully.qualified.MainClass> [program args...]" >&2
	echo "Example: $0 coppercore.standalone.TransformJsonDump" >&2
	exit 1
fi

main_class="$1"
shift

cd "${repo_root}"

if [ "$#" -gt 0 ]; then
	gradle_args=""
	for arg in "$@"; do
		printf -v gradle_args '%s%q ' "${gradle_args}" "${arg}"
	done
	gradle_args="${gradle_args% }"
	exec ./gradlew -q :standalone:run -PmainClass="${main_class}" --args="${gradle_args}"
else
	exec ./gradlew -q :standalone:run -PmainClass="${main_class}"
fi
