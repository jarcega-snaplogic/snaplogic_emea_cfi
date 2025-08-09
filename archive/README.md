# Archived Test Pipelines

This directory contains test and development pipelines moved from the root directory during cleanup.

**IMPORTANT**: Only pipelines in the root directory sync with SnapLogic. Business pipelines remain at root level for proper CI/CD synchronization.

## Structure

### test-pipelines/
Development and testing pipelines archived during cleanup:
- Various CSV/JSON transformation tests
- Sales and reporting pipeline prototypes  
- Text-to-speech testing pipeline
- Broken/incomplete test files

## Root Directory Contents

The root directory contains:
- **Production Business Pipelines**: AgentDriver*, AgentWorker*, Shippeo*, etc. (synced with SnapLogic)
- **CI/CD Demo Pipelines**: SampleCICDPipeline.slp, SampleDataProcessingPipeline.slp

## Usage

Archived test pipelines can be restored to active development by moving them back to the root directory if needed. However, only root-level pipelines will sync with SnapLogic through the CI/CD system.