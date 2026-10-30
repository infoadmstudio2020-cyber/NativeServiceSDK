#!/bin/bash
# Publish script — tags release for JitPack
set -e

VERSION=${1:-1.0.0}
echo "Tagging release v$VERSION for JitPack..."
git tag -a "v$VERSION" -m "Release v$VERSION"
git push origin "v$VERSION"
echo "Tagged v$VERSION — JitPack will build on first dependency request."
