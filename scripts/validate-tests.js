#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

const REPO_ROOT = path.dirname(__dirname);
const CONCEPTS_DIR = path.join(REPO_ROOT, 'docs', 'basics', 'caching');

console.log('Validating understanding-caching concepts...');
console.log(`Repository root: ${REPO_ROOT}`);
console.log('');

try {
    // Find all concept directories (00-*, 01-*, etc.)
    const allDirs = fs.readdirSync(CONCEPTS_DIR, { withFileTypes: true });
    const conceptDirs = allDirs
        .filter(dirent => dirent.isDirectory() && /^\d{2}-/.test(dirent.name))
        .map(dirent => dirent.name)
        .sort();

    for (const conceptName of conceptDirs) {
        const conceptDir = path.join(CONCEPTS_DIR, conceptName);

        console.log(`Testing concept: ${conceptName}`);

        // Test Java implementation
        const javaDir = path.join(conceptDir, 'java');
        if (fs.existsSync(javaDir)) {
            console.log('  Running Java tests...');
            const result = spawnSync('mvn', ['test', '-q'], {
                cwd: javaDir,
                stdio: 'inherit',
                shell: true
            });
            if (result.status !== 0) {
                console.error(`Failed: ${conceptName} (Java) - Java tests failed`);
                process.exit(1);
            }
        } else {
            console.log('  No java directory found, skipping Java tests');
        }

        // Test Python implementation
        const pythonDir = path.join(conceptDir, 'python');
        if (fs.existsSync(pythonDir)) {
            console.log('  Running Python tests...');
            // Install requirements if present
            const requirementsPath = path.join(pythonDir, 'requirements.txt');
            if (fs.existsSync(requirementsPath)) {
                const installResult = spawnSync('pip', ['install', '-q', '-r', 'requirements.txt'], {
                    cwd: pythonDir,
                    stdio: 'inherit',
                    shell: true
                });
                if (installResult.status !== 0) {
                    console.error(`Failed: ${conceptName} (Python) - pip install failed`);
                    process.exit(1);
                }
            }
            const result = spawnSync('python', ['-m', 'pytest', '-v', '--tb=short'], {
                cwd: pythonDir,
                stdio: 'inherit',
                shell: true
            });
            if (result.status !== 0) {
                console.error(`Failed: ${conceptName} (Python) - Python tests failed`);
                process.exit(1);
            }
        } else {
            console.log('  No python directory found, skipping Python tests');
        }

        console.log(`Passed: ${conceptName}`);
        console.log('');
    }

    console.log('All concepts validated successfully!');
    console.log('');
    console.log('You have demonstrated understanding of caching concepts.');
    console.log('Ready to apply this knowledge in real systems!');
} catch (error) {
    console.error('Error during validation:', error.message);
    process.exit(1);
}
