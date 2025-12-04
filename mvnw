#!/bin/sh
# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script for UN*X
# This script allows running Maven via the Maven Wrapper.
# It will download the Maven Wrapper JAR if necessary, then use it to
# download and run the configured Maven distribution.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Copyright 2014-2024 The Apache Software Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# You may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ----------------------------------------------------------------------------

# Resolve the location of the script, handling symlinks
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done

BASEDIR=`dirname "$PRG"`
BASEDIR=`cd "$BASEDIR" >/dev/null; pwd`

# OS specific support
cygwin=false
darwin=false
mingw=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true ;;
  MSYS*) mingw=true ;;
  Darwin*) darwin=true ;;
esac

# For Cygwin, ensure paths are in UNIX format
if $cygwin ; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$M2_HOME" ] && M2_HOME=`cygpath --unix "$M2_HOME"`
fi

# Locate Java executable
if [ -z "$JAVA_HOME" ] ; then
  JAVACMD="java"
else
  if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
    JAVACMD="$JAVA_HOME/jre/sh/java"
  else
    JAVACMD="$JAVA_HOME/bin/java"
  fi
fi

# Verify Java command exists
if ! command -v "$JAVACMD" >/dev/null 2>&1; then
  echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
  exit 1
fi

WRAPPER_DIR="$BASEDIR/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
WRAPPER_PROPS="$WRAPPER_DIR/maven-wrapper.properties"

# Function to download files using wget or curl
download() {
  url="$1"
  dest="$2"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$dest" "$url"
    return $?
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$dest" "$url"
    return $?
  else
    return 127
  fi
}

# Ensure wrapper jar exists
if [ ! -f "$WRAPPER_JAR" ]; then
  if [ ! -f "$WRAPPER_PROPS" ]; then
    echo "ERROR: $WRAPPER_PROPS not found. Please check your project setup." >&2
    exit 1
  fi
  WRAPPER_URL=`sed -n 's/^wrapperUrl=//p' "$WRAPPER_PROPS"`
  if [ -z "$WRAPPER_URL" ]; then
    echo "ERROR: wrapperUrl not set in $WRAPPER_PROPS" >&2
    exit 1
  fi
  mkdir -p "$WRAPPER_DIR"
  echo "Downloading Maven Wrapper from: $WRAPPER_URL"
  if ! download "$WRAPPER_URL" "$WRAPPER_JAR"; then
    echo "ERROR: Failed to download $WRAPPER_URL" >&2
    exit 1
  fi
fi

# Build classpath and execute wrapper
# shellcheck disable=SC2086
exec "$JAVACMD" \
  $MAVEN_OPTS \
  -classpath "$WRAPPER_JAR" \
  org.apache.maven.wrapper.MavenWrapperMain \
  "$@"
