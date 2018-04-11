package ute.webservice.voiceagent.dao;

import java.util.ArrayList;

/**
 * Created by Nathan Taylor on 4/11/2018.
 */

public interface ProceduresDAO {
    /**
     * If the data is not yet available, returns null. Otherwise, returns a list of the top-level category names.
     */
    ArrayList<String> getCategoryNames();

    /**
     * If the data is not yet available or the given category does not exist, returns null.
     * Otherwise, returns a list of the second-level descriptions.
     */
    ArrayList<String> getSubCategoryHeaders(String category);

    /**
     * If the data is not yet available or the given category or subcategory does not exist, returns null.
     * Otherwise, returns a list of third-level headers.
     */
    ArrayList<String> getExtremityHeaders(String category, String subcategory);

    /**
     * If the data is not yet available or the given category, subcategory or extremity does not exist, returns null.
     * Otherwise, returns a list of procedure descriptions.
     */
    ArrayList<String> getProcedureDescriptionsByExtremity(String category, String subcategory, String extremity);

    /**
     * Sends a request to the server to get all the procedure categoryNames and codes.
     */
    void fetchCategories();

    /**
     * Determines whether the procedure information needs to be fetched from the server.
     * @return True if the procedure information should be fetched.
     */
    boolean needsData();

    /**
     * Retrieves the code associated with the first found instance of the given description.
     * @param description The description of the procedure to find.
     * @return The code of the given procedure, or null if the given description is not a registered procedure.
     */
    String getCode(String description);

    /**
     * Determines whether a given description is associated with an extremity or a procedure.
     * @return True if the given node is an extremity. False if it does not exist or is not an extremity.
     */
    boolean isExtremity(String category, String subcategory, String description);
}
