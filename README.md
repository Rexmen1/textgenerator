# TextGen Plugin

A Minecraft Spigot plugin that allows you to generate 3D text using any block type, with optional outline effects.

## Features

- Generate 3D text using any block type
- Customizable text size and spacing
- Adjustable text thickness
- Optional outline effect with different block type
- Direction-aware text generation
- Works on blocks or in air (up to 50 blocks away)
- Multiple texts can coexist
- Individual undo history per player

## Requirements

- Java 21
- Spigot 1.21+

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server or use `/reload`

## Usage

### Basic Command

```
/textgen <text> <block> <size> <spacing> <width> <style> [outline]
```

### Parameters

- `text`: The text to generate
- `block`: Block material (e.g., STONE_BRICKS)
- `size`: Size of each letter (e.g., 4 for 4x4 blocks)
- `spacing`: Space between letters
- `width`: Thickness of letters
- `style`: Currently supports 'B' for bold
- `outline`: (Optional) Block type for outline effect

### Examples

```
# Generate text with stone bricks
/textgen HELLO STONE_BRICKS 4 2 3 B

# Generate text with outline
/textgen HELLO STONE_BRICKS 4 2 3 B OBSIDIAN
```

### Undo Command

```
/textgen undo
```

Removes the last text you generated.

## Permissions

- `textgen.use` - Allows using the /textgen command (default: op)

## Building from Source

1. Clone the repository

```bash
git clone https://github.com/yourusername/textgen.git
```

2. Build with Maven

```bash
cd textgen
mvn clean package
```

The compiled JAR will be in the `target` directory.

## Contributing

1. Fork the repository
2. Create a new branch for your feature
3. Submit a pull request

## License

[Your chosen license]

## Author

Rex

## Support

[Your support information]
