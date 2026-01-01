# System Patterns: Technical Blog & Portfolio Architecture

## System Architecture

### Overall Architecture
This is a static site architecture using Docusaurus as the content management and generation framework:

```
Content Sources (Markdown)
    ↓
Docusaurus Build Process
    ↓
Static Assets (HTML/CSS/JS)
    ↓
GitHub Pages Hosting
    ↓
CDN Delivery (Global)
```

### Component Relationships

#### Content Management Layer
- **Markdown Files**: Primary content storage in `/blog/` and `/docs/` directories
- **Front Matter**: Metadata management for SEO, categorization, and display
- **Asset Management**: Images, diagrams, and code examples in `/static/` directory

#### Build & Generation Layer
- **Docusaurus Core**: Static site generation engine
- **Plugin System**: Extended functionality through official and custom plugins
- **Theme System**: Customizable UI components and styling

#### Hosting & Delivery Layer
- **GitHub Pages**: Static file hosting with CDN capabilities
- **Domain Configuration**: Custom domain with SSL certificate
- **CI/CD Pipeline**: Automated deployment via GitHub Actions

## Key Technical Decisions

### Static Site Generation
**Decision**: Use Docusaurus for static site generation instead of dynamic frameworks
**Rationale**:
- Superior performance and SEO compared to dynamic sites
- Cost-effective hosting on GitHub Pages
- Better security (no server-side vulnerabilities)
- Simplified deployment and maintenance

**Trade-offs Accepted**:
- No real-time features or user-generated content
- Limited personalization capabilities
- Content must be authored in Markdown

### Content Structure
**Decision**: Hierarchical organization with `/docs/` for evergreen content and `/blog/` for time-sensitive posts
**Rationale**:
- Clear separation between reference documentation and news/timeline content
- Better SEO and discoverability for different content types
- Easier maintenance and content strategy execution

### Single Repository Architecture
**Decision**: All content, configuration, and build scripts in single GitHub repository
**Rationale**:
- Simplified deployment pipeline
- Version control for all site assets
- Easier collaboration and review processes

## Design Patterns in Use

### Content Organization Patterns

#### Progressive Disclosure
- **Pattern**: Start with overview, provide increasing detail through navigation
- **Implementation**: Sidebar navigation with nested categories
- **Benefits**: Improved user experience and information architecture

#### Front Matter Metadata
- **Pattern**: Structured metadata for content classification and SEO
- **Implementation**: YAML front matter in all Markdown files
- **Benefits**: Automated categorization, search optimization, and content management

### Build & Deployment Patterns

#### GitOps Deployment
- **Pattern**: Infrastructure and deployment managed through Git
- **Implementation**: GitHub Pages deployment via `yarn deploy`
- **Benefits**: Version-controlled deployments, audit trails, rollback capability

#### Plugin Architecture
- **Pattern**: Extensible functionality through plugin system
- **Implementation**: Mermaid diagrams, KaTeX math rendering, ideal image optimization
- **Benefits**: Modular feature development, community ecosystem access

### Performance Patterns

#### Asset Optimization
- **Pattern**: Automatic optimization of images, CSS, and JavaScript
- **Implementation**: Docusaurus build process with minification and compression
- **Benefits**: Fast loading times, reduced bandwidth usage

#### CDN Delivery
- **Pattern**: Global content delivery through CDN
- **Implementation**: GitHub Pages CDN infrastructure
- **Benefits**: Reduced latency, improved global performance

## Critical Implementation Paths

### Content Creation Workflow
1. **Authoring**: Write Markdown content with front matter
2. **Asset Management**: Add images/diagrams to appropriate directories
3. **Local Testing**: Run `yarn start` for preview
4. **Version Control**: Commit changes with descriptive messages
5. **Deployment**: Push to main branch triggers GitHub Pages deployment

### Build Process
1. **Content Parsing**: Markdown processed with remark/rehype plugins
2. **Asset Processing**: Images optimized, CSS/JS minified
3. **Routing Generation**: Static routes created for all content
4. **SEO Optimization**: Meta tags, sitemaps, and structured data generated
5. **Output**: Static files written to `/build/` directory

### Search Functionality
1. **Index Generation**: Content indexed at build time
2. **Client-side Search**: JavaScript-powered search interface
3. **Result Filtering**: Category and tag-based filtering
4. **Performance**: Pre-computed search index for fast queries

## Architectural Trade-offs (Do Not Re-debate)

### Static vs Dynamic
**Decision Made**: Static site generation prioritized over dynamic features
**Rationale**: Performance, security, and simplicity outweigh dynamic capabilities for this use case
**Consequence**: No user accounts, comments, or real-time features

### Single Page Application Trade-offs
**Decision Made**: Traditional multi-page site over SPA architecture
**Rationale**: Better SEO, faster initial page loads, simpler architecture
**Consequence**: Page transitions are full reloads rather than client-side routing

### Markdown-only Content
**Decision Made**: Markdown as exclusive content format
**Rationale**: Developer-friendly, version control friendly, portable
**Consequence**: Limited rich text editing capabilities, no WYSIWYG authoring

### GitHub Pages Hosting
**Decision Made**: GitHub Pages as hosting platform
**Rationale**: Free, integrated with repository, sufficient performance
**Consequence**: Limited server-side customization, dependent on GitHub infrastructure

## Component Architecture

### Theme Customization
- **Base Theme**: Docusaurus classic theme as foundation
- **Custom Components**: Specialized components in `/src/theme/`
- **Styling**: CSS modules and custom CSS for branding
- **Responsiveness**: Mobile-first responsive design patterns

### Content Types
- **Blog Posts**: Time-based content with social features
- **Documentation**: Versioned, searchable reference content
- **Portfolio**: Showcase content with professional presentation
- **Code Examples**: Syntax-highlighted, runnable code samples

### Navigation Patterns
- **Hierarchical Sidebar**: For documentation sections
- **Tag-based Navigation**: For blog content discovery
- **Breadcrumb Navigation**: For deep content hierarchies
- **Search-first Design**: Prominent search functionality

## Performance Architecture

### Build-time Optimizations
- **Code Splitting**: Automatic chunking of JavaScript bundles
- **Image Optimization**: Automatic resizing and format conversion
- **CSS Optimization**: Minification and critical CSS extraction
- **Font Loading**: Optimized web font loading strategies

### Runtime Optimizations
- **Lazy Loading**: Images and components loaded on demand
- **Caching**: Browser caching headers for static assets
- **Compression**: Gzip compression for text-based assets
- **CDN**: Global distribution for reduced latency
