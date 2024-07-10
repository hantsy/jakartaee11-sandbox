@TableGenerator(
        //name="id_gen", // optional
        pkColumnName = "key",
        valueColumnName = "value",
        pkColumnValue = "id",
        allocationSize = 25
)
package com.example.id;

import jakarta.persistence.TableGenerator;