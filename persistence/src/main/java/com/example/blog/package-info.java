@SequenceGenerator(name = "blog_seq", initialValue = 1, allocationSize = 10)
@TableGenerator(name = "tbl_id_gen",
        table = "id_gen",
        pkColumnName = "gen_key",
        pkColumnValue = "id",
        valueColumnName = "gen_val",
        allocationSize = 10
)
package com.example.blog;

import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.TableGenerator;