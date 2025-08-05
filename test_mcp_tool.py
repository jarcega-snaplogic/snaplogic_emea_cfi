#!/usr/bin/env python3
"""
Test script for SnapLogic CI/CD MCP tool
"""

import sys
import os
sys.path.append('mcp-tools/snaplogic-cicd')

from validators import PipelineValidator
from git_integration import GitIntegration

def test_validator():
    """Test pipeline validation functionality."""
    print("=== Testing Pipeline Validator ===")
    
    validator = PipelineValidator()
    
    # Test with ProductSalesJoinPipeline
    result = validator.validate('ProductSalesJoinPipeline.slp', detailed=True)
    print(f"ProductSalesJoinPipeline validation:")
    print(f"  Valid: {result['valid']}")
    print(f"  Errors: {len(result['errors'])}")
    print(f"  Warnings: {len(result['warnings'])}")
    
    if result['errors']:
        print("  Error details:")
        for error in result['errors']:
            print(f"    - {error}")
    
    if result['warnings']:
        print("  Warning details:")
        for warning in result['warnings']:
            print(f"    - {warning}")
    
    if result.get('details'):
        print(f"  Details: {result['details']}")
    
    print()

def test_git_integration():
    """Test Git integration functionality."""
    print("=== Testing Git Integration ===")
    
    git = GitIntegration('.')
    
    # List pipeline files
    pipelines = git.list_pipeline_files()
    print(f"Pipeline files found: {pipelines}")
    
    # Get current branch
    branch = git.get_current_branch()
    print(f"Current branch: {branch}")
    
    # Check if it's a git repo
    is_repo = git.is_git_repo()
    print(f"Is Git repository: {is_repo}")
    
    print()

def test_validation_summary():
    """Test validation summary functionality."""
    print("=== Testing Validation Summary ===")
    
    validator = PipelineValidator()
    summary = validator.get_validation_summary('.')
    
    print(f"Total pipelines: {summary['total_pipelines']}")
    print(f"Valid pipelines: {summary['valid_pipelines']}")
    print(f"Invalid pipelines: {summary['invalid_pipelines']}")
    print(f"Pipelines with warnings: {summary['pipelines_with_warnings']}")
    
    print("Pipeline details:")
    for detail in summary['details'][:5]:  # Show first 5
        print(f"  {detail['file']}: Valid={detail['valid']}, Errors={detail['error_count']}, Warnings={detail['warning_count']}")
    
    if len(summary['details']) > 5:
        print(f"  ... and {len(summary['details']) - 5} more")
    
    print()

if __name__ == "__main__":
    print("SnapLogic CI/CD MCP Tool - Test Suite")
    print("=" * 50)
    
    test_validator()
    test_git_integration()
    test_validation_summary()
    
    print("Test completed!")