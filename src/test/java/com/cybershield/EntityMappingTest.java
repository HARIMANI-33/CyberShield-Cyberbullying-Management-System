package com.cybershield;

import com.cybershield.model.AbusiveWord;
import com.cybershield.model.Complaint;
import com.cybershield.model.ComplaintAnalysis;
import com.cybershield.model.Evidence;
import com.cybershield.model.User;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EntityMappingTest {

    @Test
    void testCoreEntitiesHaveValidJpaMappings() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", "false")
                .build();

        try {
            MetadataSources sources = new MetadataSources(registry);
            sources.addAnnotatedClass(User.class);
            sources.addAnnotatedClass(Complaint.class);
            sources.addAnnotatedClass(AbusiveWord.class);
            sources.addAnnotatedClass(ComplaintAnalysis.class);
            sources.addAnnotatedClass(Evidence.class);

            Metadata metadata = sources.buildMetadata();
            assertNotNull(metadata.getEntityBinding(User.class.getName()));
            assertNotNull(metadata.getEntityBinding(Complaint.class.getName()));
            assertNotNull(metadata.getEntityBinding(AbusiveWord.class.getName()));
            assertNotNull(metadata.getEntityBinding(ComplaintAnalysis.class.getName()));
            assertNotNull(metadata.getEntityBinding(Evidence.class.getName()));
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
