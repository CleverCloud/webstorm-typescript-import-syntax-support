# TypeScript Import Syntax Support

<!-- Plugin description -->
A WebStorm/IntelliJ plugin that adds navigation and documentation support for TypeScript's `@import` comment-based type imports in JavaScript and TypeScript files.

Enables Ctrl+Click navigation to type definitions and module files, plus hover documentation for imported types.
<!-- Plugin description end -->

## Features

- **🔍 Type Navigation**: Ctrl+Click (Cmd+Click on Mac) on imported types to jump to their definitions
- **📁 File Navigation**: Navigate to imported modules by clicking on the import path
- **📖 Type Hints**: Hover over imported types to see their definitions and documentation
- **📦 Multiple Imports**: Support for multiple `@import` statements in a single comment block
- **🔗 Transitive Imports**: Automatically resolve types re-exported through multiple files
- **📂 Relative Paths**: Full support for relative paths (`./`, `../`)

## Installation

### Prerequisites

- JDK 17 or higher (recommended: JDK 21)
- Git

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/CleverCloud/webstorm-typescript-import-syntax-support.git
   cd webstorm-typescript-import-syntax-support
   ```

2. **Build the plugin**:
   ```bash
   ./gradlew buildPlugin
   ```

3. **Locate the plugin file**:
   The plugin ZIP file will be generated at:
   ```
   build/distributions/typescript-import-syntax-support-<version>.zip
   ```

### Installing in Your IDE

1. Open **Settings/Preferences** → **Plugins**
2. Click **⚙️** (gear icon) → **Install Plugin from Disk...**
3. Select the generated ZIP file from `build/distributions/`
4. Restart your IDE when prompted

## Usage

Use TypeScript's `@import` syntax in your JavaScript files:

```javascript
/**
 * @import {User, Role} from './types.d.ts'
 * @import {IconModel} from '../common/types.d.ts'
 */

/** @type {User} */
const user = {
  name: "John Doe",
  role: "admin"
};

/** @type {IconModel} */
const icon = {
  content: "svg-data"
};
```

### Supported Scenarios

✅ TypeScript interfaces and type aliases
✅ Named imports with aliases (`Type as Alias`)
✅ Parent directory (`../`) and subdirectory imports
✅ Transitive type resolution through re-exports
✅ Multiple types from the same module

## Examples

### Basic Import
```javascript
/**
 * @import {User} from './user.types.d.ts'
 */
```

### Multiple Imports in One Comment
```javascript
/**
 * @import {User} from './user.types.d.ts'
 * @import {Product} from './product.types.d.ts'
 * @import {Order} from '../orders/order.types.d.ts'
 */
```

### Multiple Types from Same Module
```javascript
/**
 * @import {User, Role, Permission} from './auth.types.d.ts'
 */
```

### Import with Alias
```javascript
/**
 * @import {User as UserType} from './types.d.ts'
 */
```

### Transitive Imports
If `intermediate.types.d.ts` re-exports types from `base.types.d.ts`:

```javascript
// base.types.d.ts
export interface BaseType {
  id: string;
}

// intermediate.types.d.ts
/**
 * @import {BaseType} from './base.types.d.ts'
 */

// main.js
/**
 * @import {BaseType} from './intermediate.types.d.ts'
 */
// ✅ BaseType will be resolved through the transitive import
```

## Development

### Building the Plugin

```bash
./gradlew buildPlugin
```

### Running Tests

```bash
./gradlew test
```

### Running the Plugin in a Sandbox IDE

```bash
./gradlew runIde
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built with the [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- Supports TypeScript's [@import syntax](https://devblogs.microsoft.com/typescript/announcing-typescript-5-3/#import-attributes)