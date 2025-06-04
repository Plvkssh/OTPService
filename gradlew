#!/usr/bin/env sh

# Gradle wrapper execution script
# Maintains original functionality while improving readability

set -e

# ----- Initial Setup -----
resolve_app_home() {
    local app_path=$1
    while [ -h "$app_path" ]; do
        local ls_output=$(ls -ld "$app_path")
        local link_target=${ls_output#*' -> '}
        case $link_target in
            /*) app_path=$link_target ;;
            *) app_path="${APP_HOME}$link_target" ;;
        esac
        APP_HOME=${app_path%"${app_path##*/}"}
    done
    APP_HOME=$(cd -P "${APP_HOME:-./}" >/dev/null && pwd -P) || exit 1
}

setup_environment() {
    # System type detection
    case $(uname) in
        CYGWIN*) SYSTEM_TYPE=cygwin ;;
        Darwin*) SYSTEM_TYPE=darwin ;;
        MSYS*|MINGW*) SYSTEM_TYPE=msys ;;
        NONSTOP*) SYSTEM_TYPE=nonstop ;;
        *) SYSTEM_TYPE=posix ;;
    esac
}

# ----- Core Functions -----
validate_java() {
    if [ -n "$JAVA_HOME" ]; then
        if [ -x "$JAVA_HOME/jre/sh/java" ]; then
            JAVA_CMD="$JAVA_HOME/jre/sh/java"
        else
            JAVA_CMD="$JAVA_HOME/bin/java"
        fi
        [ -x "$JAVA_CMD" ] || die "Invalid JAVA_HOME: $JAVA_HOME"
    else
        JAVA_CMD=java
        command -v java >/dev/null 2>&1 || die "Java not found in PATH"
    fi
}

adjust_file_descriptors() {
    [ "$SYSTEM_TYPE" = "cygwin" ] || [ "$SYSTEM_TYPE" = "darwin" ] || [ "$SYSTEM_TYPE" = "nonstop" ] && return
    
    local max_fd
    max_fd=$(ulimit -H -n 2>/dev/null) || warn "Cannot query max file descriptors"
    
    [ -n "$max_fd" ] && [ "$max_fd" != "unlimited" ] && ulimit -n "$max_fd" 2>/dev/null || 
        warn "Cannot set file descriptor limit"
}

prepare_arguments() {
    if [ "$SYSTEM_TYPE" = "cygwin" ] || [ "$SYSTEM_TYPE" = "msys" ]; then
        APP_HOME=$(cygpath --mixed "$APP_HOME")
        CLASSPATH=$(cygpath --mixed "$CLASSPATH")
        JAVA_CMD=$(cygpath --unix "$JAVA_CMD")
    fi
}

# ----- Main Execution -----
main() {
    # Initialize variables
    local APP_BASE_NAME=${0##*/}
    local CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
    local DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
    
    # Setup environment
    resolve_app_home "$0"
    setup_environment
    
    # Validate Java installation
    validate_java
    
    # System configuration
    adjust_file_descriptors
    
    # Prepare execution arguments
    prepare_arguments
    
    # Execute Gradle
    set -- \
        "-Dorg.gradle.appname=$APP_BASE_NAME" \
        -classpath "$CLASSPATH" \
        org.gradle.wrapper.GradleWrapperMain \
        "$@"
    
    exec "$JAVA_CMD" "$@"
}

# Helper functions
warn() { echo "$*" >&2; }
die() { echo >&2; echo "$*" >&2; echo >&2; exit 1; }

# Start main execution
main "$@"
