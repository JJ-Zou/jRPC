package com.zjj.remoting;

import java.util.List;

public interface ChildListener {

    void childChanged(String path, List<String> children);

}
