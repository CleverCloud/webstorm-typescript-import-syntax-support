# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-10-20

### Added
- Initial release of TypeScript @import Syntax Support plugin
- Type navigation: Ctrl+Click on imported types to jump to definitions
- File navigation: Click on import paths to open the referenced file
- Hover documentation: View type definitions by hovering over imported types
- Support for multiple @import statements in a single comment block
- Transitive import resolution: Automatically resolve types re-exported through multiple files
- Full relative path support (`./`, `../`)
- Support for TypeScript interfaces and type aliases
- Named imports with aliases (`Type as Alias`) syntax
- Parent directory and subdirectory imports
- Multiple types from the same module
- Comprehensive test suite with 10 test cases

### Technical Details
- Compatible with WebStorm 2024.3.6+
- Built on IntelliJ Platform SDK
- Uses PSI (Program Structure Interface) for robust code analysis
- Implements custom reference providers for type and module resolution