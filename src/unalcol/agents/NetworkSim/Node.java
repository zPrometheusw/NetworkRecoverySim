/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unalcol.agents.NetworkSim;

import unalcol.agents.Agent;
import unalcol.agents.AgentArchitecture;
import unalcol.agents.AgentProgram;

/**
 *
 * @author ARODRIGUEZ
 */
public class Node extends Agent {
    private GraphElements.MyVertex v;

    public Node(AgentArchitecture _architecture, AgentProgram _program) {
        super(_architecture, _program);
    }
}
