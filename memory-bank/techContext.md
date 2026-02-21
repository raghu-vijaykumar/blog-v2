# Tech Context: Technical Blog & Portfolio

## Technology Stack

### Core Framework
- **Docusaurus**: React-based static site generator (v3.9.2)
- **React**: Frontend framework (v19.0.0)
- **TypeScript**: Type-safe JavaScript development
- **Node.js**: Runtime environment (≥20.0)

### Build & Development Tools
- **Yarn**: Package management and build orchestration
- **Webpack**: Module bundling (via Docusaurus)
- **Babel**: JavaScript transpilation
- **PostCSS**: CSS processing and optimization

### Content & Documentation
- **MDX**: Markdown with JSX support for rich content
- **Remark/Rehype**: Markdown processing plugins
- **KaTeX**: Mathematical notation rendering
- **Mermaid**: Diagram generation and rendering

### Hosting & Deployment
- **GitHub Pages**: Static site hosting
- **GitHub Actions**: CI/CD pipeline (implied)
- **Git**: Version control system

## Development Setup

### Local Development Environment
```bash
# Prerequisites
Node.js >= 20.0
Yarn package manager
Git for version control

# Installation
yarn install

# Development server
yarn start

# Build for production
yarn build

# Serve production build locally
yarn serve
```

### Project Structure
```
/
├── blog/                 # Blog posts (Markdown)
├── docs/                 # Documentation pages
├── src/                  # React components and themes
├── static/               # Static assets (images, etc.)
├── docusaurus.config.ts  # Site configuration
├── package.json          # Dependencies and scripts
├── sidebars.ts          # Documentation navigation
└── tsconfig.json        # TypeScript configuration
```

### Development Workflow
1. **Feature Branch**: Create branch for new content/features
2. **Local Development**: Run `yarn start` for hot reloading
3. **Content Creation**: Write Markdown in appropriate directories
4. **Asset Management**: Place images in `/static/` directory
5. **Testing**: Verify build with `yarn build`
6. **Code Review**: Push branch and create pull request
7. **Deployment**: Merge to main triggers GitHub Pages deployment

### Preview / Verification Behavior (Durable)
- Do not auto-open the browser or files after content edits. Previews are user-initiated only.
- The dev server is configured with `--no-open` (see package.json). Start it with `yarn start` (or `npm run start`) and manually navigate to the site in your browser.
- Local URL: `http://localhost:3000/blog-v2/` (respects `baseUrl` from docusaurus.config.ts).
- Automation and AI agents must not launch browsers or run OS-level `open` commands automatically; provide instructions instead and wait for explicit approval.

## Technical Constraints

### Hosting Limitations
- **Static Only**: No server-side processing or databases
- **GitHub Pages Limits**: 1GB storage, 100GB bandwidth/month
- **Build Time**: Must complete within GitHub Actions time limits
- **Custom Domain**: Must use GitHub's custom domain feature

### Performance Requirements
- **Core Web Vitals**: Meet Google's performance standards
- **Lighthouse Score**: Target 90+ across all categories
- **Mobile Performance**: Optimize for mobile networks
- **SEO**: Achieve high search engine rankings

### Browser Support
- **Modern Browsers**: Chrome, Firefox, Safari, Edge (latest 2 versions)
- **Mobile Browsers**: iOS Safari, Chrome Mobile
- **Progressive Enhancement**: Graceful degradation for older browsers

### Content Constraints
- **Markdown Only**: No rich text editors or WYSIWYG
- **Static Assets**: All images/diagrams must be pre-generated
- **No Dynamic Content**: All content generated at build time
- **File Size Limits**: Optimize assets for web delivery

## Dependencies & Libraries

### Core Dependencies
```json
{
  "@docusaurus/core": "3.9.2",
  "@docusaurus/preset-classic": "3.9.2",
  "@docusaurus/theme-mermaid": "^3.9.2",
  "react": "^19.0.0",
  "react-dom": "^19.0.0",
  "clsx": "^2.0.0",
  "prism-react-renderer": "^2.3.0"
}
```

### Content Processing
- **remark-math**: Mathematical notation in Markdown
- **rehype-katex**: KaTeX rendering for math expressions
- **@docusaurus/theme-mermaid**: Diagram rendering

### Development Dependencies
- **TypeScript**: Type checking and compilation
- **ESLint**: Code linting (implied through Docusaurus)
- **Prettier**: Code formatting (implied)

## Tooling & Workflow Conventions

### Version Control
- **Git Flow**: Main branch for production, feature branches for development
- **Conventional Commits**: Structured commit messages
- **Pull Requests**: Required for all changes
- **Code Reviews**: Mandatory peer review process

### Code Quality
- **TypeScript Strict**: Strict type checking enabled
- **ESLint**: Automated code linting in CI/CD
- **Prettier**: Consistent code formatting
- **Testing**: Basic build verification (expandable)

### Content Management
- **Front Matter**: YAML metadata for all content
- **File Naming**: kebab-case for directories and files
- **Image Optimization**: Automated through Docusaurus
- **SEO Optimization**: Meta tags and structured data
 - **Heading Numbering**: Authors write plain Markdown headings using # for H1 (page title), ## for H2, etc. Automatic visual numbering is applied site-wide via CSS counters for H2–H6. Do not hardcode numbers in headings. H1 is intentionally not numbered. To opt out for a specific heading in MDX, use JSX: `<h3 className="no-number">Heading</h3>`.
 - **Depth & Examples**: Each Markdown page should go beyond definitions—explain What/Why/When/How, include at least one concrete example when introducing a new concept, and provide code or relevant snippets when describing implementations. Use the deep‑dive structure for larger examples.
 - **Cross‑links**: Avoid adding trailing “Related modules” sections; rely on sidebar/navigation ordering. Add inline links only when they materially aid comprehension.
 - **MDX Safety**: Wrap angle‑bracket tokens like `<id>` in backticks to avoid MDX JSX parsing issues; prefer fenced code blocks for snippets.

### Fundamentals content policy (Durable)
- Fundamentals pages default to “doc‑only” with examples and diagrams; colocated runnable code is OFF by default.
- Inline snippets are allowed only when they clarify policy/algorithms (pseudocode/SQL/YAML/config), each ≤ 30 lines.
- Require ≥ 2 worked examples per fundamentals page (one quantitative, one architectural) and at least one Mermaid diagram when topology/flows benefit.
- Opt‑in to the Docs + Code deep‑dive structure only when explicitly requested.

### Docs + Code Deep‑Dive Structure (Durable Convention)
- When a topic needs implementation details, convert the page into a folder and rename the page file to `readme.mdx` while preserving the public route via a `slug` frontmatter field.
- Colocate runnable example code with minimal dependencies under language-specific subfolders.

Directory pattern (generic)
```
docs/<domain>/<section>/<topic>/
  readme.mdx                 # main page; include `slug` to keep route stable
  <impl-name>/               # e.g., lru, lfu, arc, clock
    README.md                # implementation notes + links back to readme.mdx
    code/
      python/*.py            # self-contained; tiny test in __main__
      java/*.java            # self-contained; tiny test in main()
      [optional] go/*.go, js/*.mjs, etc.
```

Linking conventions
- In readme.mdx, add an “Implementation examples” section linking to each `<impl-name>/README.md` and example code using relative paths (e.g., `./lru/code/python/lru.py`).
- Prefer short, focused examples that can be run directly (no frameworks unless necessary).

When not to deep‑dive
- Fundamentals/overview pages meant to teach concepts rather than implementations.
- When code adds noise without improving understanding.

### Deployment Pipeline
```yaml
# GitHub Actions workflow (implied)
name: Deploy to GitHub Pages
on:
  push:
    branches: [main]
jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: 20
      - run: yarn install
      - run: yarn build
      - uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./build
```

## Environment Assumptions

### Development Environment
- **Operating System**: Cross-platform (Windows, macOS, Linux)
- **Node.js Version**: 20.x LTS or higher
- **Package Manager**: Yarn 1.x or npm
- **Git**: Latest stable version
- **Browser**: Modern browser with development tools

### Production Environment
- **Hosting Platform**: GitHub Pages
- **Domain**: Custom domain via GitHub settings
- **SSL**: Automatic HTTPS via GitHub
- **CDN**: Built-in GitHub Pages CDN

### Network Assumptions
- **Internet Connectivity**: Required for development and deployment
- **GitHub Access**: Repository access and API availability
- **CDN Availability**: Reliable content delivery network

### Security Assumptions
- **Static Site**: No server-side vulnerabilities
- **HTTPS Only**: All traffic served over secure connection
- **Dependency Security**: Regular dependency updates and security audits
- **Content Security**: Markdown-only content prevents XSS attacks

## Build Configuration

### Docusaurus Configuration (`docusaurus.config.ts`)
- **Base URL**: `/blog-v2/` for GitHub Pages subdirectory
- **Organization**: `raghu-vijaykumar`
- **Project**: `blog-v2`
- **Theme**: Classic preset with customizations

### TypeScript Configuration (`tsconfig.json`)
- **Target**: ES2017 for broad browser support
- **Module**: ESNext with bundler resolution
- **Strict Mode**: Full TypeScript strict checking
- **JSX**: React JSX transform

### Styling Approach
- **CSS Modules**: Scoped styling for components
- **Custom CSS**: Global styles in `/src/css/custom.css`
- **Theme Customization**: Component-level theme overrides
- **Responsive Design**: Mobile-first CSS approach
 - **Automatic Heading Numbering**: Implemented with CSS counters in `src/css/custom.css` for docs and blog content (`.theme-doc-markdown`, `.markdown`) to render hierarchical numbers (e.g., 1., 1.1, 1.1.1) for H2–H6.

## Performance Optimization

### Build Optimizations
- **Code Splitting**: Automatic chunking by Docusaurus
- **Asset Optimization**: Image compression and format conversion
- **CSS Optimization**: Minification and vendor prefixing
- **JavaScript Optimization**: Minification and dead code elimination

### Runtime Optimizations
- **Lazy Loading**: Images loaded on demand
- **Caching**: Aggressive browser caching for static assets
- **Compression**: Gzip compression for text assets
- **CDN**: Global distribution for reduced latency

### Monitoring & Analytics
- **Lighthouse**: Performance monitoring tool
- **Web Vitals**: Core Web Vitals tracking
- **Bundle Analyzer**: JavaScript bundle size analysis
- **SEO Tools**: Search Console and analytics tracking
