#include <gtest/gtest.h>
#include "Simulator.h"
#include "model/Node.h"

using namespace hson;

namespace hson {
    class MockNode final : public Node {
    };
}


TEST(ScheduleQueue, EmptyQueue) {
    const ScheduleQueue queue;
    EXPECT_TRUE(queue.empty());
}

TEST(ScheduleQueue, PushNode) {
    ScheduleQueue queue;
    for (int i = 0; i != 10; ++i) {
        const auto ptr = new MockNode;
        ptr->setTopology(10 - i);
        queue.push(ptr);
    }

    int index = 1;
    while (!queue.empty()) {
        const auto node = queue.poll();
        EXPECT_EQ(node->getTopology(), index++);
        delete node;
    }

    EXPECT_TRUE(queue.empty());
}

TEST(ScheduleQueue, Duplication) {
    ScheduleQueue queue;
    std::vector<Node *> nodes;
    for (int i = 0; i != 10; ++i) {
        const auto ptr = new MockNode;
        ptr->setTopology(i);
        nodes.push_back(ptr);
    }
    for (int i = 0; i != 10; ++i) {
        queue.push(nodes[i]);
    }
    for (int i = 0; i != 10; ++i) {
        queue.push(nodes[i]);
    }
    for (int i = 0; i != 10; ++i) {
        queue.push(nodes[i]);
    }
    int index = 0;
    while (!queue.empty()) {
        const auto node = queue.poll();
        EXPECT_EQ(node->getTopology(), index++);
        delete node;
    }

    EXPECT_TRUE(queue.empty());
}

