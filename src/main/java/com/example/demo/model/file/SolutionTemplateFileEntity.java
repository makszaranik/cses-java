package com.example.demo.model.file;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@SuperBuilder
@Document("files")
@NoArgsConstructor
@TypeAlias("solution_template")
@EqualsAndHashCode(callSuper = true)
public class SolutionTemplateFileEntity extends FileEntity {
}
