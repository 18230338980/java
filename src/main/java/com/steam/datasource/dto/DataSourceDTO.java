package com.steam.datasource.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author TaoYu
 * @date 2020/1/27
 */
@Data
public class DataSourceDTO {

  @NotBlank
  @ApiModelProperty(value = "连接池名称", example = "master_2")
  private String pollName;

  @NotBlank
  @ApiModelProperty(value = "JDBC driver", example = "com.mysql.cj.jdbc.Driver")
  private String driverClassName;

  @NotBlank
  @ApiModelProperty(value = "JDBC url 地址", example = "jdbc:mysql://127.0.0.1:3306/test?characterEncoding=utf-8&serverTimezone=UTC&useSSL=false")
  private String url;

  @NotBlank
  @ApiModelProperty(value = "JDBC 用户名", example = "root")
  private String username;

  @ApiModelProperty(value = "JDBC 密码")
  private String password;

}
