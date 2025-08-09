/**
 * Intelligent caching module for SnapLogic snap schemas
 * Optimizes memory usage and API calls with multi-index caching
 */

export class SchemaCache {
  constructor() {
    // Compact catalog storage - only essential fields
    this.catalog = new Map(); // class_id → {name, category, description, version}
    
    // Fast lookup indexes
    this.byCategory = new Map(); // category → Set of class_ids
    this.searchIndex = new Map(); // token → Set of class_ids
    
    
    // Cache metadata
    this.catalogLastFetch = null;
    this.catalogTTL = 24 * 60 * 60 * 1000; // 24 hours
    this.isLoading = false;
  }

  /**
   * Check if catalog needs refresh
   */
  needsCatalogRefresh() {
    if (!this.catalogLastFetch) return true;
    const age = Date.now() - this.catalogLastFetch;
    return age > this.catalogTTL;
  }

  /**
   * Process and index the catalog response
   * Extracts only essential fields to minimize memory usage
   */
  processCatalog(apiResponse) {
    this.catalog.clear();
    this.byCategory.clear();
    this.searchIndex.clear();
    
    // Handle the actual API response structure
    const snapData = apiResponse.response_map || apiResponse;
    
    // Process each snap directly from response_map
    for (const [classId, snapSchema] of Object.entries(snapData)) {
      // Skip if this doesn't look like a snap schema
      if (!snapSchema || typeof snapSchema !== 'object' || !snapSchema.class_map) {
        continue;
      }
      
      // Extract snap name from class_map info
      let snapName = classId;
      let description = snapSchema.description || '';
      
      if (snapSchema.class_map && snapSchema.class_map.info) {
        snapName = snapSchema.class_map.info.label?.value || snapName;
        description = snapSchema.class_map.info.notes?.value || description;
      }
      
      // Determine category from class_id
      const categoryMatch = classId.match(/com-snaplogic-snaps-([^-]+)/);
      const category = categoryMatch ? categoryMatch[1] : 'unknown';
      
      if (!this.byCategory.has(category)) {
        this.byCategory.set(category, new Set());
      }
      
      // Extract essential fields only
      const compactSnap = {
        name: snapName,
        category: category,
        description: description,
        version: snapSchema.class_version || 1
      };
      
      // Store in catalog
      this.catalog.set(classId, compactSnap);
      
      // Add to category index
      this.byCategory.get(category).add(classId);
      
      // Build search index
      this.indexSnapForSearch(classId, compactSnap);
    }
    
    this.catalogLastFetch = Date.now();
  }

  /**
   * Build search index for a snap
   */
  indexSnapForSearch(classId, snap) {
    const text = `${snap.name} ${snap.description}`.toLowerCase();
    const tokens = this.tokenize(text);
    
    for (const token of tokens) {
      if (!this.searchIndex.has(token)) {
        this.searchIndex.set(token, new Set());
      }
      this.searchIndex.get(token).add(classId);
    }
  }

  /**
   * Simple tokenization for search
   */
  tokenize(text) {
    return text
      .split(/[\s\-_\.]+/)
      .filter(token => token.length > 2)
      .map(token => token.toLowerCase());
  }

  /**
   * Search snaps by query using contains-based matching
   */
  searchSnaps(query, category = null) {
    const searchQuery = query.toLowerCase().trim();
    
    // Return empty results for very short queries
    if (searchQuery.length < 2) {
      return [];
    }
    
    const matches = new Map(); // class_id → score
    
    // Search through all catalog entries
    for (const [classId, snapInfo] of this.catalog) {
      // Skip if category filter doesn't match
      if (category && snapInfo.category !== category.toLowerCase()) continue;
      
      // Create searchable text from all available fields
      const searchableText = `${classId} ${snapInfo.name} ${snapInfo.description}`.toLowerCase();
      
      // Check if searchable text contains the query
      if (searchableText.includes(searchQuery)) {
        let score = 1; // Base score for contains match
        
        // Split into words for better matching analysis
        const words = searchableText.split(/[\s\-_\.]+/).filter(w => w.length > 0);
        
        // Higher score for exact word match
        if (words.includes(searchQuery)) {
          score = 5;
        }
        // Higher score for word that starts with query
        else if (words.some(word => word.startsWith(searchQuery))) {
          score = 3;
        }
        // Bonus if query appears in class_id (more relevant)
        if (classId.toLowerCase().includes(searchQuery)) {
          score += 1;
        }
        // Bonus if query appears in display name
        if (snapInfo.name.toLowerCase().includes(searchQuery)) {
          score += 1;
        }
        
        matches.set(classId, score);
      }
    }
    
    // Sort by score (descending) then alphabetically by class_id
    const results = Array.from(matches.entries())
      .sort((a, b) => {
        const scoreDiff = b[1] - a[1];
        return scoreDiff !== 0 ? scoreDiff : a[0].localeCompare(b[0]);
      })
      .map(([classId]) => ({
        class_id: classId,
        ...this.catalog.get(classId)
      }));
    
    return results;
  }


  /**
   * Get all categories with counts
   */
  getCategories() {
    const categories = [];
    for (const [category, snaps] of this.byCategory) {
      categories.push({
        name: category,
        count: snaps.size
      });
    }
    return categories.sort((a, b) => a.name.localeCompare(b.name));
  }

  /**
   * Get basic info for a snap
   */
  getSnapInfo(classId) {
    return this.catalog.get(classId) || null;
  }

  /**
   * Clear all caches
   */
  clear() {
    this.catalog.clear();
    this.byCategory.clear();
    this.searchIndex.clear();
    this.catalogLastFetch = null;
  }
}