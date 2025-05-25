#!/bin/bash

# SketchID Test Coverage Runner
# This script helps you run unit test coverage and view reports

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_color() {
    printf "${2}${1}${NC}\n"
}

# Function to print section headers
print_header() {
    echo ""
    print_color "========================================" "$BLUE"
    print_color "$1" "$BLUE"
    print_color "========================================" "$BLUE"
    echo ""
}

# Function to show coverage report locations
show_report_locations() {
    echo ""
    print_color "📊 Coverage Reports Generated:" "$GREEN"
    echo ""
    
    if [ -d "app/build/reports/jacoco/test/html" ]; then
        print_color "📋 Unit Test Coverage: app/build/reports/jacoco/test/html/index.html" "$YELLOW"
    fi
    
    echo ""
    print_color "💡 Tip: Open the HTML files in your browser to view detailed coverage reports" "$BLUE"
    echo ""
}

# Function to run unit test coverage
run_unit_coverage() {
    print_header "Running Unit Test Coverage"
    print_color "This will run all unit tests and generate coverage report..." "$YELLOW"
    
    ./gradlew unitTestCoverage
    
    print_color "✅ Unit test coverage completed successfully!" "$GREEN"
    show_report_locations
}

# Function to clean coverage reports
clean_coverage() {
    print_header "Cleaning Coverage Reports"
    print_color "Removing all existing coverage reports..." "$YELLOW"
    
    ./gradlew cleanCoverageReports
    
    print_color "✅ Coverage reports cleaned successfully!" "$GREEN"
}

# Function to show help
show_help() {
    print_header "SketchID Coverage Runner Help"
    
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  unit      - Run unit tests and generate coverage report"
    echo "  clean     - Clean all existing coverage reports"
    echo "  help      - Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 unit          # Run unit test coverage"
    echo "  $0 clean         # Clean coverage reports"
    echo ""
    print_color "🔍 Coverage reports are generated as HTML files you can open in a browser" "$BLUE"
}

# Function to show current test info
show_test_info() {
    print_header "SketchID Test Information"
    
    echo "📊 Current Test Structure:"
    echo ""
    echo "Unit Tests (app/src/test/):"
    if [ -d "app/src/test/java/com/mfrankic/sketchid" ]; then
        unit_count=$(find app/src/test/java/com/mfrankic/sketchid -name "*Test.java" | wc -l)
        print_color "  📋 $unit_count unit test files" "$GREEN"
    fi
    
    echo ""
    print_color "💡 Run '$0 help' for coverage commands" "$BLUE"
}

# Main script logic
case "$1" in
    "unit")
        run_unit_coverage
        ;;
    "clean")
        clean_coverage
        ;;
    "help")
        show_help
        ;;
    "info")
        show_test_info
        ;;
    "")
        show_test_info
        echo ""
        show_help
        ;;
    *)
        print_color "❌ Unknown command: $1" "$RED"
        echo ""
        show_help
        exit 1
        ;;
esac 
