package ca.utoronto.tdccbr.services.enrichmentmap.model;

public interface Columns {
	
	public static final String NAMESPACE = "EnrichmentMap";
	public static final String NAMESPACE_PREFIX = NAMESPACE + "::";
	
	// ---[ GeneMANIA Attributes ]--------------------------------------------------------------------------------------
	// NODE
	ColumnDescriptor<String> GM_GENE_NAME = new ColumnDescriptor<>("gene name", String.class);
	ColumnDescriptor<String> GM_QUERY_TERM = new ColumnDescriptor<>("query term", String.class);
	
	// ---[ STRING Attributes ]-----------------------------------------------------------------------------------------
	// NODE
	ColumnDescriptor<String> STR_GENE_NAME = new ColumnDescriptor<>("display name", String.class);
	ColumnDescriptor<String> STR_QUERY_TERM = new ColumnDescriptor<>("query term", String.class);
	
	// ---[ EM Attributes ]---------------------------------------------------------------------------------------------
	// Common attributes that apply to the entire network
	public static final ColumnDescriptor<String> NODE_NAME = new ColumnDescriptor<>("Name", String.class);
	public static final ColumnDescriptor<String> NODE_GS_DESCR = new ColumnDescriptor<>("GS_DESCR", String.class);
	public static final ColumnDescriptor<String> NODE_DAVID_CATEGORY = new ColumnDescriptor<>("david_category", String.class);
	public static final ColumnDescriptor<String> NODE_DATASOURCE = new ColumnDescriptor<>("GS_datasource", String.class);
	public static final ColumnDescriptor<String> NODE_DATASOURCEID = new ColumnDescriptor<>("GS_datasource_id", String.class);
	public static final ColumnDescriptor<String> NODE_GS_TYPE  = new ColumnDescriptor<>("GS_Type", String.class);
	public static final String NODE_GS_TYPE_ENRICHMENT = "ENR";
	public static final String NODE_GS_TYPE_SIGNATURE  = "SIG";
	public static final ColumnListDescriptor<String> NODE_GENES = new ColumnListDescriptor<>("Genes", String.class);
	public static final ColumnDescriptor<Integer> NODE_GS_SIZE  = new ColumnDescriptor<>("gs_size", Integer.class);
	
	// Per-DataSet attributes
	// GSEA attributes
	public static final ColumnDescriptor<Double> NODE_PVALUE      = new ColumnDescriptor<>("pvalue", Double.class);
	public static final ColumnDescriptor<Double> NODE_FDR_QVALUE  = new ColumnDescriptor<>("fdr_qvalue", Double.class);
	public static final ColumnDescriptor<Double> NODE_FWER_QVALUE = new ColumnDescriptor<>("fwer_qvalue", Double.class);
	public static final ColumnDescriptor<Double> NODE_ES          = new ColumnDescriptor<>("ES", Double.class);
	public static final ColumnDescriptor<Double> NODE_NES         = new ColumnDescriptor<>("NES", Double.class);
	public static final ColumnDescriptor<Double> NODE_COLOURING   = new ColumnDescriptor<>("Colouring", Double.class);
	
	// Post-analysis Node attributes
	public static final ColumnListDescriptor<String> NODE_ENR_GENES = new ColumnListDescriptor<>("Enrichment_Genes", String.class);
	
	// Per-DataSet attributes
	// Edge attributes
	public static final ColumnDescriptor<Double> EDGE_SIMILARITY_COEFF = new ColumnDescriptor<>("similarity_coefficient", Double.class);
	public static final ColumnDescriptor<Integer> EDGE_OVERLAP_SIZE = new ColumnDescriptor<>("Overlap_size", Integer.class);
	public static final ColumnListDescriptor<String> EDGE_OVERLAP_GENES = new ColumnListDescriptor<>("Overlap_genes", String.class);
	
	// Multi-edge case
	public static final ColumnDescriptor<String> EDGE_DATASET = new ColumnDescriptor<>("Data Set", String.class);
	public static final String EDGE_DATASET_VALUE_COMPOUND = "compound";
	public static final String EDGE_DATASET_VALUE_SIG = "signature"; // post-analysis edges
	public static final String EDGE_INTERACTION_VALUE_OVERLAP = "Geneset_Overlap";
	public static final String EDGE_INTERACTION_VALUE_SIG = "sig"; // post-analysis edges
	public static final ColumnDescriptor<String> EDGE_SIG_DATASET = new ColumnDescriptor<>("Signature Set", String.class);
	
	// Post-analysis Edge Attributes
	public static final ColumnDescriptor<Double> EDGE_HYPERGEOM_PVALUE = new ColumnDescriptor<>("Overlap_Hypergeom_pVal", Double.class);
	public static final ColumnDescriptor<Double> EDGE_HYPERGEOM_CUTOFF = new ColumnDescriptor<>("Overlap_Hypergeom_cutoff", Double.class);
	public static final ColumnDescriptor<Integer> EDGE_HYPERGEOM_U = new ColumnDescriptor<>("HyperGeom_N_Universe", Integer.class);
	public static final ColumnDescriptor<Integer> EDGE_HYPERGEOM_N = new ColumnDescriptor<>("HyperGeom_n_Sig_Universe", Integer.class);
	public static final ColumnDescriptor<Integer> EDGE_HYPERGEOM_K = new ColumnDescriptor<>("k_Intersection", Integer.class);
	public static final ColumnDescriptor<Integer> EDGE_HYPERGEOM_M = new ColumnDescriptor<>("m_Enr_Genes", Integer.class);
	public static final ColumnDescriptor<Double> EDGE_MANN_WHIT_TWOSIDED_PVALUE = new ColumnDescriptor<>("Overlap_Mann_Whit_pVal", Double.class);
	public static final ColumnDescriptor<Double> EDGE_MANN_WHIT_GREATER_PVALUE = new ColumnDescriptor<>("Overlap_Mann_Whit_greater_pVal", Double.class);
	public static final ColumnDescriptor<Double> EDGE_MANN_WHIT_LESS_PVALUE = new ColumnDescriptor<>("Overlap_Mann_Whit_less_pVal", Double.class);
	public static final ColumnDescriptor<Double> EDGE_MANN_WHIT_CUTOFF = new ColumnDescriptor<>("Overlap_Mann_Whit_cutoff", Double.class);
	public static final ColumnDescriptor<String> EDGE_CUTOFF_TYPE = new ColumnDescriptor<>("Overlap_cutoff", String.class);
	
	/** Column in edge table that holds the formula */
	public static final ColumnDescriptor<Double> EDGE_WIDTH_FORMULA_COLUMN = new ColumnDescriptor<>("Edge_width_formula", Double.class);
	/** Column in network table that holds the edge parameters */
	public static final ColumnDescriptor<String> NETWORK_EDGE_WIDTH_PARAMETERS_COLUMN = new ColumnDescriptor<>("EM_Edge_width_parameters", String.class);
	
	public static final ColumnDescriptor<String> NET_REPORT1_DIR = new ColumnDescriptor<>("GSEA_Report_Dataset1_folder", String.class);
	public static final ColumnDescriptor<String> NET_REPORT2_DIR = new ColumnDescriptor<>("GSEA_Report_Dataset2_folder", String.class);
	
	public static final ColumnListDescriptor<Integer> DATASET_CHART = new ColumnListDescriptor<>("Dataset_Chart", Integer.class);
	public static final ColumnListDescriptor<Double> EXPRESSION_DATA_CHART = new ColumnListDescriptor<>("Expression_Data_Chart", Double.class);
		
}