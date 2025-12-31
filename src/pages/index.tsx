import type {ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import useBaseUrl from '@docusaurus/useBaseUrl';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';

import styles from './index.module.css';

function PortfolioHeader() {
  const profileImageUrl = useBaseUrl('img/profile.jpg');
  return (
    <header className={clsx('hero', styles.portfolioHeader)}>
      <div className="container">
        <div className={styles.headerContent}>
          <div className={styles.profileImage}>
            {/* Replace 'profile.jpg' with your actual profile image path */}
            <img
              src={profileImageUrl}
              alt="Raghu Vijaykumar"
              className={styles.profileImg}
            />
          </div>
          <div className={styles.headerText}>
            <Heading as="h1" className={styles.name}>
              Raghu Vijaykumar
            </Heading>
            <p className={styles.title}>
              Staff Software Engineer | Cloud, Data & AI
            </p>
            <div className={styles.socialLinks}>
              <a href="https://github.com/raghu-vijaykumar" target="_blank" rel="noopener noreferrer" className={styles.socialLink}>
                GitHub
              </a>
              <a href="https://leetcode.com/u/raghuvijaykumar/" target="_blank" rel="noopener noreferrer" className={styles.socialLink}>
                LeetCode
              </a>
              <a href="https://www.linkedin.com/in/raghuvijaykumar/" target="_blank" rel="noopener noreferrer" className={styles.socialLink}>
                LinkedIn
              </a>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}

function AboutSection() {
  return (
    <section className={styles.about}>
      <div className="container">
        <Heading as="h2">About Me</Heading>
        <p>
          I'm a Staff Software Engineer with 9+ years of experience building large-scale cloud platforms, distributed systems, and data-driven applications across healthcare, fintech, aviation, and telecom domains.
        </p>
        <p>
          My work spans Cloud (GCP, AWS, OCI), Data Engineering, and AI-powered systems, with a strong focus on designing resilient architectures, high-throughput pipelines, and automation that meaningfully reduces operational effort. I enjoy working close to complex systems—where performance, reliability, and correctness matter—and turning them into simple, maintainable solutions.
        </p>
        <p>
          In recent roles, I've:
        </p>
        <ul>
          <li>Designed and operated multi-cloud, high-availability platforms handling 100M+ daily transactions and $10M/day revenue</li>
          <li>Built AI-assisted tools and agentic workflows that reduced hours of manual work to minutes</li>
          <li>Led and mentored engineers while delivering cross-team initiatives with strong engineering standards</li>
          <li>Scaled data ingestion and analytics pipelines processing 100TB+ datasets using Beam, Dataflow, streaming systems, and modern warehouses</li>
          <li>Strengthened security, observability, and CI/CD practices across enterprise systems</li>
        </ul>
        <p>
          Outside work, I actively build AI tools, automation CLIs, and experimental products, and I write about distributed systems and real-world engineering trade-offs on my blog. I'm driven by curiosity, clean design, and building systems that scale both technically and operationally.
        </p>
      </div>
    </section>
  );
}

function SkillsSection() {
  return (
    <section className={styles.skills}>
      <div className="container">
        <Heading as="h2">Skills & Expertise</Heading>

        <div className={styles.skillGroup}>
          <Heading as="h3">Core Technologies</Heading>
          <div className={styles.skillTags}>
            <span className={styles.skillTag}>Java & Spring</span>
            <span className={styles.skillTag}>Python</span>
            <span className={styles.skillTag}>Flutter</span>
            <span className={styles.skillTag}>React</span>
            <span className={styles.skillTag}>Node.js</span>
          </div>
        </div>

        <div className={styles.skillGroup}>
          <Heading as="h3">Cloud & Infrastructure</Heading>
          <div className={styles.skillTags}>
            <span className={styles.skillTag}>Google Cloud Platform</span>
            <span className={styles.skillTag}>AWS</span>
            <span className={styles.skillTag}>Oracle Cloud</span>
            <span className={styles.skillTag}>Kubernetes</span>
            <span className={styles.skillTag}>Docker</span>
          </div>
        </div>

        <div className={styles.skillGroup}>
          <Heading as="h3">Data & AI</Heading>
          <div className={styles.skillTags}>
            <span className={styles.skillTag}>Apache Beam</span>
            <span className={styles.skillTag}>BigQuery</span>
            <span className={styles.skillTag}>LangChain</span>
            <span className={styles.skillTag}>Machine Learning</span>
            <span className={styles.skillTag}>LLMs</span>
          </div>
        </div>

        <div className={styles.skillGroup}>
          <Heading as="h3">DevOps & Tools</Heading>
          <div className={styles.skillTags}>
            <span className={styles.skillTag}>Terraform</span>
            <span className={styles.skillTag}>Jenkins</span>
            <span className={styles.skillTag}>Git</span>
            <span className={styles.skillTag}>Monitoring</span>
            <span className={styles.skillTag}>Security</span>
          </div>
          <p>
            View my cloud certifications on{' '}
            <a href="https://github.com/raghu-vijaykumar/course-certificates/tree/main/cloud" target="_blank" rel="noopener noreferrer">
              GitHub
            </a>
          </p>
        </div>
      </div>
    </section>
  );
}

function ProjectsSection() {
  return (
    <section className={styles.projects}>
      <div className="container">
        <Heading as="h2">Personal Projects</Heading>
        <div className={styles.projectList}>
          <div className={styles.project}>
            <h3><a href="https://github.com/raghu-vijaykumar/tools" target="_blank" rel="noopener noreferrer">AI Tools Monorepo</a></h3>
            <p>Built a Python UV-based monorepo unifying newsletter summarization (RSS → multi-LLM summaries → Telegram) and RAG-driven documentation generation with reusable modules for embeddings, vector stores, LLM abstraction, and audio processing, delivering a single CLI automation layer that improved daily workflow efficiency.</p>
          </div>
          <div className={styles.project}>
            <h3><a href="https://github.com/raghu-vijaykumar/PaperToReel" target="_blank" rel="noopener noreferrer">Paper To Reel</a></h3>
            <p>Creating a pipeline that converts comic books into short storytelling videos using OCR for text extraction, LangGraph & LLMs for context generation, text-to-speech for narration, and video generation tools. Samples <a href="https://www.youtube.com/@PlotBlitz" target="_blank" rel="noopener noreferrer">here</a>.</p>
          </div>
          <div className={styles.project}>
            <h3><a href="https://raghu-vijaykumar.github.io/blog/" target="_blank" rel="noopener noreferrer">My Blog</a></h3>
            <p>Built a personal knowledge-sharing platform using Hugo and GitHub Pages; published 5+ long-form posts on distributed systems & engineering challenges.</p>
          </div>
          <div className={styles.project}>
            <h3><a href="https://github.com/raghu-vijaykumar/banter" target="_blank" rel="noopener noreferrer">Banter</a></h3>
            <p>Engineered a Flutter mobile app displaying jokes from Reddit & other sources with translation features; exploratory project comparing Flutter UI/UX with web frameworks.</p>
          </div>
        </div>
      </div>
    </section>
  );
}



function EducationSection() {
  return (
    <section className={styles.education}>
      <div className="container">
        <Heading as="h2">Education</Heading>
        <div className={styles.educationItem}>
          <div className={styles.educationHeader}>
            <h3>Visveswaraya Technological University</h3>
            <span className={styles.dates}>June 2012 – May 2016</span>
          </div>
          <p>Bachelor of Engineering, Electronics & Communication</p>
          <p className={styles.location}>Mysuru, Karnataka, India</p>
          <ul>
            <li>Project: LDPC Codes for Near-Earth Space communication programmed using HDL at ISRO Satellite Centre, Bengaluru.</li>
            <li>Internship: BSNL EETP program on telecom equipment spanning 6 months with 3 certification levels.</li>
          </ul>
        </div>
      </div>
    </section>
  );
}

function LatestBlogsSection() {
  // Blog posts data - easily maintainable format
  // Update this array when you add new blog posts (keep most recent at top)
  const blogPosts = [
    {
      slug: 'using-ollama-for-note-generation-locally',
      title: 'Using Ollama for Note Generation Locally',
      date: '2024-08-26',
      description: 'Using Ollama library to run and connect to models locally for generating readable and easy-to-understand notes from transcripts.'
    },
    {
      slug: 'enterprise-product-things-to-consider',
      title: 'Enterprise Product - Things to consider',
      date: '2024-08-26',
      description: 'Developing an enterprise product is a complex endeavour that demands meticulous planning and execution. Here\'s a comprehensive checklist that can serve as your roadmap to success.'
    },
    {
      slug: 'ensuring-resiliency-high-availability-disaster-recovery',
      title: 'Ensuring Resiliency: High Availability and Disaster Recovery Strategies',
      date: '2024-08-26',
      description: 'High Availability (HA) and Disaster Recovery (DR) are critical strategies in cloud computing to ensure services are continuously available and resilient against failures.'
    },
    {
      slug: 'product-template-documentation',
      title: 'Product Template - Documentation',
      date: '2024-08-26',
      description: 'Tech Product Documentation Template serves as a comprehensive framework for organizing and communicating essential information about a technology product.'
    },
    {
      slug: 'algorithmic-techniques',
      title: 'Algorithmic Techniques',
      date: '2024-08-26',
      description: 'Algorithmic techniques are strategies used to design efficient algorithms for solving computational problems. Here are some of the most common and powerful algorithmic techniques.'
    }
  ];

  // Take only the first 5 posts (most recent)
  const latestPosts = blogPosts.slice(0, 3).map(post => ({
    ...post,
    permalink: `/blog/${post.slug}`
  }));

  return (
    <section className={styles.blogs}>
      <div className="container">
        <Heading as="h2">Latest Blog Posts</Heading>
        <div className={styles.blogList}>
          {latestPosts.map((post) => (
            <div key={post.permalink} className={styles.blogPost}>
              <h3>
                <Link to={post.permalink}>{post.title}</Link>
              </h3>
              <p className={styles.blogDate}>
                {new Date(post.date).toLocaleDateString('en-US', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric'
                })}
              </p>
              {post.description && (
                <p className={styles.blogExcerpt}>{post.description}</p>
              )}
              <Link to={post.permalink} className={styles.readMore}>
                Read more →
              </Link>
            </div>
          ))}
        </div>
        <div className={styles.viewAll}>
          <Link to="/blog" className="button button--primary">
            View All Posts
          </Link>
        </div>
      </div>
    </section>
  );
}

export default function Home(): ReactNode {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title="Raghu Vijaykumar - Staff Software Engineer"
      description="Portfolio of Raghu Vijaykumar, Staff Software Engineer specializing in Cloud, Data & AI solutions">
      <PortfolioHeader />
      <main>
        <section className={styles.aboutAndBlogs}>
          <div className="container">
            <div className={styles.twoColumnLayout}>
              <div className={styles.aboutColumn}>
                <AboutSection />
              </div>
              <div className={styles.blogsColumn}>
                <LatestBlogsSection />
              </div>
            </div>
          </div>
        </section>
        <ProjectsSection />
      </main>
    </Layout>
  );
}
