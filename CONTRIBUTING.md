# Contributing to Autonome

Thank you for your interest in contributing to Autonome! We welcome contributions from the community.

## How to Contribute

### Reporting Bugs

If you find a bug, please open an issue on GitHub with:
- A clear, descriptive title
- Steps to reproduce the issue
- Expected vs actual behavior
- Your environment (Java version, Spring Boot version, OS)
- Code samples or error logs if applicable

### Suggesting Features

Feature requests are welcome! Please open an issue with:
- A clear description of the feature
- Why it would be useful
- Examples of how it would work
- Any implementation ideas you have

### Pull Requests

We love pull requests! Here's how to submit one:

1. **Fork the repository** and create your branch from `main`
   ```bash
   git checkout -b feature/my-new-feature
   ```

2. **Make your changes** following our coding standards:
   - Follow existing code style and conventions
   - Add tests for new functionality
   - Update documentation as needed
   - Keep commits focused and atomic

3. **Test your changes**
   ```bash
   mvn clean test
   ```

4. **Commit your changes** with clear commit messages:
   ```bash
   git commit -m "feat: add new agent configuration option"
   ```

5. **Push to your fork** and submit a pull request:
   ```bash
   git push origin feature/my-new-feature
   ```

### Code Style Guidelines

- Follow standard Java conventions
- Use meaningful variable and method names
- Add Javadoc comments for public APIs
- Keep methods focused and under 50 lines when possible
- Use Spring Boot best practices
- Write unit tests for new functionality (aim for 80%+ coverage)

### Commit Message Format

We follow conventional commits:
- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation changes
- `test:` - Adding or updating tests
- `refactor:` - Code refactoring
- `chore:` - Maintenance tasks

Example: `feat: add Redis-based context store with TTL support`

## Development Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Spring Boot 3.4+
- Docker (optional, for Redis testing)

### Building from Source

```bash
# Clone the repository
git clone https://github.com/aibroughttolife/autonome-framework.git
cd autonome-framework

# Build all modules
mvn clean install

# Run tests
mvn test

# Skip tests (faster build)
mvn clean install -DskipTests
```

### Running Examples

```bash
cd autonome-community
mvn spring-boot:run
```

## Project Structure

```
autonome-parent/
├── autonome-api/          # Core API definitions
├── autonome-core/         # Implementation & engine
└── autonome-community/    # Demo agents and examples
```

## Testing

- Write unit tests for all new code
- Integration tests for complex workflows
- Example flows should be tested end-to-end
- Run all tests before submitting PR: `mvn test`

## Documentation

When adding new features:
- Update relevant README sections
- Add Javadoc comments for public APIs
- Create or update example flows
- Update architecture diagrams if needed

## Community

- Be respectful and inclusive
- Follow our Code of Conduct
- Ask questions in GitHub Discussions
- Help others when you can

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

## Questions?

- 💬 GitHub Discussions: Ask questions
- 🐛 GitHub Issues: Report bugs
- 📧 Email: For private inquiries

Thank you for contributing to Autonome! 🚀