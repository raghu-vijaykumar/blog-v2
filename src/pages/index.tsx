import type {ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';

import styles from './index.module.css';

function PortfolioHeader() {
  return (
    <header className={clsx('hero', styles.portfolioHeader)}>
      <div className="container">
        <div className={styles.headerContent}>
          <div className={styles.profileImage}>
            {/* Replace 'profile.jpg' with your actual profile image path */}
            <img
              src="/img/profile.jpg"
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
            <div className={styles.contact}>
              raghu.dinka.vijaykumar@gmail.com
            </div>
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
        <Heading as="h2">About</Heading>
        <p>
          Software Engineer with 9 years of experience building scalable cloud, distributed systems, and data platforms.
          Currently leading AI/ML initiatives at Oracle's Healthcare AI team, previously drove enterprise analytics solutions at Equifax,
          and built high-throughput systems at Boeing. Proven track record in designing resilient architectures, optimizing performance,
          and driving AI/ML-powered innovation. Passionate about solving complex problems with clean, impactful solutions.
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
  // All migrated blog posts - sorted by date (most recent first)
  const latestPosts = [
    {
      title: 'Using Ollama for Note Generation Locally',
      date: '2024-08-26',
      permalink: '/blog/2024/08/26/using-ollama-for-note-generation-locally',
      description: 'Using Ollama library to run and connect to models locally for generating readable and easy-to-understand notes from transcripts.'
    },
    {
      title: 'Enterprise Product - Things to consider',
      date: '2024-08-26',
      permalink: '/blog/2024/08/26/enterprise-product-things-to-consider',
      description: 'Developing an enterprise product is a complex endeavour that demands meticulous planning and execution. Here\'s a comprehensive checklist that can serve as your roadmap to success.'
    },
    {
      title: 'Ensuring Resiliency: High Availability and Disaster Recovery Strategies',
      date: '2024-08-26',
      permalink: '/blog/2024/08/26/ensuring-resiliency-high-availability-disaster-recovery',
      description: 'High Availability (HA) and Disaster Recovery (DR) are critical strategies in cloud computing to ensure services are continuously available and resilient against failures.'
    },
    {
      title: 'Product Template - Documentation',
      date: '2024-08-26',
      permalink: '/blog/2024/08/26/product-template-documentation',
      description: 'Tech Product Documentation Template serves as a comprehensive framework for organizing and communicating essential information about a technology product.'
    },
    {
      title: 'Algorithmic Techniques',
      date: '2024-08-26',
      permalink: '/blog/2024/08/26/algorithmic-techniques',
      description: 'Algorithmic techniques are strategies used to design efficient algorithms for solving computational problems. Here are some of the most common and powerful algorithmic techniques.'
    },
    {
      title: 'DSA - Sliding Window - (In Progress)',
      date: '2024-08-26',
      permalink: '/blog/2024/08/26/sliding-window',
      description: 'The sliding window technique is a powerful approach used to solve a variety of problems, especially those involving subarrays or substrings.'
    },
    {
      title: 'DSA - Two Pointer Approach - (In Progress)',
      date: '2024-08-26',
      permalink: '/blog/2024/08/26/two-pointer-approach',
      description: 'The two-pointer approach is a powerful technique used to solve problems involving arrays or strings efficiently.'
    }
  ];

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
        <AboutSection />
        <SkillsSection />
        <ProjectsSection />
        <EducationSection />
        <LatestBlogsSection />
      </main>
    </Layout>
  );
}
