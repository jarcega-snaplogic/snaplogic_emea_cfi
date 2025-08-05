"""
Git integration utilities for SnapLogic CI/CD operations.
Handles Git operations like committing, status checking, and file management.
"""

import os
import subprocess
from pathlib import Path
from typing import List, Dict, Any
import glob


class GitIntegration:
    """Git operations for SnapLogic pipeline management."""
    
    def __init__(self, repo_path: str = "."):
        """Initialize Git integration."""
        self.repo_path = Path(repo_path)
        
    def list_pipeline_files(self, pattern: str = "*.slp") -> List[str]:
        """
        List all pipeline files in the repository.
        
        Args:
            pattern: File pattern to match (default: *.slp)
            
        Returns:
            List of pipeline file names (without extension)
        """
        pipeline_files = glob.glob(str(self.repo_path / pattern))
        return [os.path.basename(f).replace('.slp', '') for f in pipeline_files]
    
    def commit_changes(self, files: List[str], commit_message: str, auto_add: bool = True) -> Dict[str, Any]:
        """
        Commit changes to Git repository.
        
        Args:
            files: List of files to commit
            commit_message: Commit message
            auto_add: Whether to add files automatically
            
        Returns:
            Result dictionary with status
        """
        try:
            if auto_add:
                # Add specified files
                for file in files:
                    if os.path.exists(file):
                        subprocess.run(['git', 'add', file], check=True, cwd=self.repo_path)
            
            # Create commit
            full_message = f"{commit_message}\\n\\n🤖 Generated with [Claude Code](https://claude.ai/code)\\n\\nCo-Authored-By: Claude <noreply@anthropic.com>"
            
            result = subprocess.run(
                ['git', 'commit', '-m', full_message],
                capture_output=True,
                text=True,
                cwd=self.repo_path
            )
            
            if result.returncode == 0:
                return {
                    'status': 'success',
                    'message': f'Successfully committed {len(files)} file(s)',
                    'commit_hash': self._get_latest_commit_hash(),
                    'files': files
                }
            else:
                return {
                    'status': 'error',
                    'message': f'Git commit failed: {result.stderr}',
                    'files': files
                }
                
        except subprocess.CalledProcessError as e:
            return {
                'status': 'error',
                'message': f'Git operation failed: {str(e)}',
                'files': files
            }
    
    def get_status(self) -> Dict[str, Any]:
        """
        Get Git repository status.
        
        Returns:
            Dictionary with repository status information
        """
        try:
            result = subprocess.run(
                ['git', 'status', '--porcelain'],
                capture_output=True,
                text=True,
                cwd=self.repo_path,
                check=True
            )
            
            lines = result.stdout.strip().split('\n') if result.stdout.strip() else []
            
            modified = []
            added = []
            deleted = []
            untracked = []
            
            for line in lines:
                if line:
                    status = line[:2]
                    filename = line[3:]
                    
                    if status.startswith('M'):
                        modified.append(filename)
                    elif status.startswith('A'):
                        added.append(filename)
                    elif status.startswith('D'):
                        deleted.append(filename)
                    elif status.startswith('??'):
                        untracked.append(filename)
            
            return {
                'status': 'success',
                'modified': modified,
                'added': added,
                'deleted': deleted,
                'untracked': untracked,
                'clean': len(lines) == 0
            }
            
        except subprocess.CalledProcessError as e:
            return {
                'status': 'error',
                'message': f'Failed to get Git status: {str(e)}'
            }
    
    def get_changed_pipelines(self, base_ref: str = "HEAD~1") -> List[str]:
        """
        Get list of pipeline files that changed since base reference.
        
        Args:
            base_ref: Base reference to compare against (default: HEAD~1)
            
        Returns:
            List of changed pipeline file names
        """
        try:
            result = subprocess.run(
                ['git', 'diff', '--name-only', base_ref, 'HEAD'],
                capture_output=True,
                text=True,
                cwd=self.repo_path,
                check=True
            )
            
            changed_files = result.stdout.strip().split('\n') if result.stdout.strip() else []
            
            # Filter for .slp files and extract names
            pipeline_changes = []
            for file in changed_files:
                if file.endswith('.slp'):
                    pipeline_name = os.path.basename(file).replace('.slp', '')
                    pipeline_changes.append(pipeline_name)
            
            return pipeline_changes
            
        except subprocess.CalledProcessError as e:
            print(f"Error getting changed files: {str(e)}")
            return []
    
    def _get_latest_commit_hash(self) -> str:
        """Get the hash of the latest commit."""
        try:
            result = subprocess.run(
                ['git', 'rev-parse', 'HEAD'],
                capture_output=True,
                text=True,
                cwd=self.repo_path,
                check=True
            )
            return result.stdout.strip()[:8]  # Short hash
        except subprocess.CalledProcessError:
            return "unknown"
    
    def is_git_repo(self) -> bool:
        """Check if current directory is a Git repository."""
        try:
            subprocess.run(
                ['git', 'rev-parse', '--git-dir'],
                capture_output=True,
                cwd=self.repo_path,
                check=True
            )
            return True
        except subprocess.CalledProcessError:
            return False
    
    def get_current_branch(self) -> str:
        """Get the current Git branch name."""
        try:
            result = subprocess.run(
                ['git', 'branch', '--show-current'],
                capture_output=True,
                text=True,
                cwd=self.repo_path,
                check=True
            )
            return result.stdout.strip()
        except subprocess.CalledProcessError:
            return "unknown"
    
    def push_to_origin(self, branch: str = None) -> Dict[str, Any]:
        """
        Push changes to origin repository.
        
        Args:
            branch: Branch to push (default: current branch)
            
        Returns:
            Result dictionary with push status
        """
        if not branch:
            branch = self.get_current_branch()
        
        try:
            result = subprocess.run(
                ['git', 'push', 'origin', branch],
                capture_output=True,
                text=True,
                cwd=self.repo_path,
                check=True
            )
            
            return {
                'status': 'success',
                'message': f'Successfully pushed to origin/{branch}',
                'branch': branch,
                'output': result.stdout
            }
            
        except subprocess.CalledProcessError as e:
            return {
                'status': 'error',
                'message': f'Failed to push to origin: {e.stderr}',
                'branch': branch
            }